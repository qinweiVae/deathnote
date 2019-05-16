package com.qinwei.deathnote.support.scan;

import com.qinwei.deathnote.config.conf.StandardConfig;
import com.qinwei.deathnote.support.watch.FileListener;
import com.qinwei.deathnote.support.watch.FileWatcher;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author qinwei
 * @date 2019-05-13
 */
@Slf4j
public class ResourcesScanner {

    public static final String APPLICATION_NAME = "application.properties";

    public static final String CONFIG_PATH = "config.path";

    public static final String PROPERTIES_PATTERN = ".properties";

    private Map<String, Object> resources = new HashMap<>();

    private ResourcesScanner() {

    }

    public static ResourcesScanner getInstance() {
        return LazyResourcesScanner.INSTANCE;
    }

    public Map<String, Object> scan() {
        Path path = scanConfigPath();
        doScan(path);
        registerFileWatch(path);
        return resources;
    }

    private void registerFileWatch(Path path) {
        FileWatcher fileWatcher = new FileWatcher(path, new FileListener() {
            @Override
            public void changed(Path dir) {
                resources.clear();
                StandardConfig.getInstance().initConfig();
            }
        }, new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(PROPERTIES_PATTERN);
            }
        });
        fileWatcher.watch();
    }

    private Map<String, Object> doScan(Path path) {
        try {
            //遍历目录下的所有properties文件
            List<File> list = walkFileTree(path);
            if (CollectionUtils.isEmpty(list)) {
                return new HashMap<>();
            }
            //application.properties 拥有最高优先级，解析的时候排在最后面，文件排在越后面，里面的配置优先级越高
            list.stream().sorted((o1, o2) -> o1.getName().endsWith(APPLICATION_NAME) ? 1 : -1)
                    .forEach(file -> {
                        Properties properties = new Properties();
                        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
                            properties.load(reader);
                            Enumeration<?> enumeration = properties.propertyNames();
                            while (enumeration.hasMoreElements()) {
                                String key = (String) enumeration.nextElement();
                                resources.put(key, properties.getProperty(key));
                            }
                        } catch (IOException e) {
                            log.error("load file failure ,file = {}", file.getPath(), e);
                        }
                    });

        } catch (IOException e) {
            log.error("scan local config failure ..", e);
        }
        return resources;
    }

    /**
     * 递归遍历文件
     */
    private List<File> walkFileTree(Path path) throws IOException {
        List<File> list = new ArrayList();
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                //只要properties文件
                if (filePath.toString().endsWith(PROPERTIES_PATTERN)) {
                    list.add(filePath.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return list;
    }

    private Path scanConfigPath() {
        //命令行参数拥有高优先级
        Path path = scanProperty();
        if (path != null) {
            return path;
        }
        //没有设置命令行参数的话，从当前classpath下查找
        String localPath = ClassUtils.getDefaultClassLoader().getResource("").getPath();
        File file = new File(localPath);
        if (file.exists()) {
            return file.toPath();
        }
        return null;
    }

    private Path scanProperty() {
        String path = System.getProperty(CONFIG_PATH);
        if (StringUtils.isNotEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                return file.toPath();
            }
        }
        return null;
    }

    private static class LazyResourcesScanner {
        private static final ResourcesScanner INSTANCE = new ResourcesScanner();
    }
}
