package com.qinwei.deathnote.support.watch;

import com.sun.nio.file.SensitivityWatchEventModifier;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * @author qinwei
 * @date 2019-05-15
 */
@Slf4j
public class WatchServiceRegister {

    private WatchService watchService;

    public static final WatchEvent.Kind[] EVENTS = {ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE};

    public WatchServiceRegister() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException("cannot build watchService", e);
        }
        closeGracefully();
    }

    private void closeGracefully() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                watchService.close();
            } catch (IOException e) {
                log.error("can not close watch service ...", e);
            }
        }));
    }


    /**
     * path注册到WatchService
     */
    public WatchService register(Path path) {
        try {
            /* path.register() 方法只能监听path下一层的文件(夹),如果创建了 path/a/c.txt 这样的文件 就不会被监听到.
             * 所以遍历path下的所有文件夹(多层)都注册到watchService
             */
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    // 提高灵敏度，提示速度明显增加
                    dir.register(watchService, EVENTS, SensitivityWatchEventModifier.HIGH);
                    return CONTINUE;
                }
            });

        } catch (IOException e) {
            log.error("path register failure ,path = {}", path, e);
        }
        return watchService;
    }

}
