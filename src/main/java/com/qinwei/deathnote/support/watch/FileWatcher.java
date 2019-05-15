package com.qinwei.deathnote.support.watch;

import com.sun.nio.file.SensitivityWatchEventModifier;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

/**
 * @author qinwei
 * @date 2019-05-15
 */
@Slf4j
public class FileWatcher implements Watcher, Runnable {

    private static ExecutorService executors = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("FileWatcherThread");
        thread.setDaemon(true);
        return thread;
    });

    private volatile boolean started = false;

    private volatile boolean isRunning = true;

    private WatchService watchService;

    private WatchKey watchKey;

    //需要监听的path
    private Path path;

    //文件监听器
    private FileListener listener;

    //文件过滤器
    private FilenameFilter filenameFilter;

    public FileWatcher(Path path, FileListener listener) {
        this(path, listener, null);
    }

    public FileWatcher(Path path, FileListener listener, FilenameFilter filenameFilter) {
        this.path = path;
        this.listener = listener;
        this.filenameFilter = filenameFilter;
    }

    @Override
    public void watch() {
        //同一个实例只能调用一次
        if (started) {
            throw new RuntimeException("The same instance can not invoke watch() twice ");
        }
        WatchServiceRegister register = new WatchServiceRegister();
        watchService = register.register(path);
        executors.execute(this);
        started = true;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                watchKey = watchService.take();
                if (watchKey == null) {
                    continue;
                }
                for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                    WatchEvent<Path> event = cast(watchEvent);
                    //注意event.context()得到的只有一个文件名,不是全路径
                    Path context = event.context();
                    //文件名过滤
                    if (filenameFilter != null && !filenameFilter.accept(path.toFile(), context.toString())) {
                        continue;
                    }
                    Path watchable = ((Path) watchKey.watchable()).resolve(context);
                    WatchEvent.Kind<?> kind = watchEvent.kind();
                    if (kind == ENTRY_CREATE) {
                        //在项目运行时又添加了一个子文件夹 path/a/b 这样如果在b中添加文件 watchService监听不到.所以在监听到文件夹创建的时候要给让这个path注册到watchService上
                        if (Files.isDirectory(watchable)) {
                            watchNewDir(watchable);
                        }
                    }
                    //listener通知
                    listener.changed(watchable);
                }
            } catch (Exception e) {
                log.error("watchService failure ", e);
            } finally {
                //每次的到新的事件后，需要重置监听池
                if (watchKey != null) {
                    watchKey.reset();
                }
            }
        }
    }

    /**
     * 当从其他地方直接复制过来多层目录结构的文件,多层目录的子目录同样不会被监控到,所以要遍历文件夹注册
     */
    private void watchNewDir(Path newDir) {
        try {
            newDir.register(watchService, WatchServiceRegister.EVENTS, SensitivityWatchEventModifier.HIGH);
        } catch (IOException e) {
            log.error("path register failure ,path = {}", newDir, e);
        }
        File[] files = newDir.toFile().listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                watchNewDir(file.toPath());
            }
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
        watchKey.cancel();
    }

    private <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }
}
