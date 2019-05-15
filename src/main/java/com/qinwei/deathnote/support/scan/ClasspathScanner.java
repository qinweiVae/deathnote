package com.qinwei.deathnote.support.scan;

import com.qinwei.deathnote.utils.ClassUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author qinwei
 * @date 2019-05-12 15:26
 */
@Slf4j
public class ClasspathScanner implements Scanner {

    private static final String CLASS_PATTERN = ".class";

    private static final char PACKAGE_SEPARATOR = '.';

    private static final char PATH_SEPARATOR = '/';

    private static final String JAR_SEPARATOR = "!";

    private static Map<String, Set<Class>> packageCache = new ConcurrentHashMap<>();

    /**
     * 根据指定包名, 扫描classpath 下和所有jar包内的class
     */
    @Override
    public Set<Class> scan(String basePackage) {
        if (basePackage.endsWith(".")) {
            basePackage = basePackage.substring(0, basePackage.length() - 1);
        }
        Set<Class> result = packageCache.get(basePackage);
        if (result == null) {
            synchronized (packageCache) {
                if (packageCache.containsKey(basePackage)) {
                    result = packageCache.get(basePackage);
                } else {
                    result = new LinkedHashSet<>();
                    doScan(basePackage, result);
                    packageCache.put(basePackage, result);
                }
            }
        }
        return result;
    }

    private void doScan(String basePackage, Set<Class> result) {
        try {
            String path = resolveBasePackage(basePackage);
            Enumeration<URL> urls = findAllClassPathResources(path);
            if (urls == null) {
                return;
            }
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String file = URLDecoder.decode(url.getFile(), "UTF-8");
                    parseClassFile(new File(file), path, result);
                } else if ("jar".equals(protocol)) {
                    parseJarFile(url, result);
                }
            }
        } catch (Exception e) {
            log.error("ClasspathScanner scan failure , basePackage = {}", basePackage, e);
        }
    }

    /**
     * 解析jar包
     */
    private void parseJarFile(URL url, Set<Class> result) {
        try {
            String[] jarInfo = url.getPath().split(JAR_SEPARATOR);
            //jar包路径
            String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
            //查找的包名
            String packagePath = jarInfo[1].substring(1);
            JarFile jarFile = new JarFile(jarFilePath);
            //JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entry.isDirectory()) {
                    continue;
                }
                if (isNormalClass(entryName) && entryName.startsWith(packagePath)) {
                    addClassToSet(result, entryName);
                }

            }
        } catch (Exception e) {
            log.error("can not get jar file ,　url :{}", url, e);
        }
    }

    /**
     * 解析classpath目录
     */
    private void parseClassFile(File file, String path, Set<Class> result) {
        if (!file.exists()) {
            return;
        }
        //不能以"/"开始，否则会有问题
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (file.isDirectory()) {
            //只要目录和class文件,并且过滤掉匿名内部类
            File[] files = file.listFiles(pathName -> pathName.isDirectory() || isNormalClass(pathName.getName()));
            for (File subFile : files) {
                parseClassFile(subFile, path + PATH_SEPARATOR + subFile.getName(), result);
            }
        } else if (isNormalClass(file.getName())) {
            addClassToSet(result, path);
        }
    }

    private void addClassToSet(Set<Class> result, String fileName) {
        //去掉.class,并把a/b/c转成a.b.c
        String className = revertBasePackage(fileName.substring(0, fileName.length() - 6));
        Class clazz = loadClass(className);
        if (clazz != null) {
            result.add(clazz);
        }
    }

    /**
     * 如果是class，并且不是匿名内部类
     */
    private boolean isNormalClass(String className) {
        return className.endsWith(CLASS_PATTERN) && !className.contains("$");
    }

    /**
     * 把a.b.c 转换成 a/b/c
     */
    private String resolveBasePackage(String basePackage) {
        return basePackage.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
    }

    /**
     * 把a/b/c 转换成 a.b.c
     */
    private String revertBasePackage(String basePackage) {
        return basePackage.replace(PATH_SEPARATOR, PACKAGE_SEPARATOR);
    }

    private Enumeration<URL> findAllClassPathResources(String path) throws IOException {
        //不能以"/"开始，否则会有问题
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return getClassLoader().getResources(path);
    }

    private ClassLoader getClassLoader() {
        return ClassUtils.getDefaultClassLoader();
    }

    private Class loadClass(String className) {
        try {
            return getClassLoader().loadClass(className);
        } catch (Exception e) {
            try {
                return ClassLoader.getSystemClassLoader().loadClass(className);
            } catch (Throwable e1) {
                log.error("can not load class , class name : {} ", className, e1);
            }
        }
        return null;
    }
}
