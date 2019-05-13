package com.qinwei.deathnote.support.scan;

import com.qinwei.deathnote.utils.ClassUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-05-13
 */
@Slf4j
public class PropertiesScanner {

    public static final String APPLICATION_NAME = "application.properties";

    public static final String CONFIG_PATH = "config.path";

    public Map<String, String> scan() {
        Path path = scanConfigPath();
        if (path == null) {
            return null;
        }
        try {
            Files.walkFileTree(path, new SimpleFileVisitor(){
                @Override
                public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) throws IOException {
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            log.error("scan local config failure ..", e);
        }
        return null;
    }

    private Path scanConfigPath() {
        Path path = scanProperty();
        if (path != null) {
            return path;
        }
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

    public static void main(String[] args) {
        System.setProperty(CONFIG_PATH, "d:/config");
        PropertiesScanner scanner = new PropertiesScanner();
        System.out.println(scanner.scanConfigPath());
    }
}
