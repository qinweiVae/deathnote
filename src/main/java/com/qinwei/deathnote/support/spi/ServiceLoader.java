package com.qinwei.deathnote.support.spi;

import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.CollectionUtils;
import com.qinwei.deathnote.utils.Holder;
import lombok.extern.slf4j.Slf4j;
import com.qinwei.deathnote.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinwei
 * @date 2019-05-17
 */
@Slf4j
public class ServiceLoader {

    private static final String SERVICES_DIRECTORY = "META-INF/services/";

    private static final Map<String, Map<String, Class>> SERVICE_CACHE = new ConcurrentHashMap<>();

    private static final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    private static <T> boolean withSpiAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
    }

    public static <T> T getService(Class<T> type) {
        return getService(type, "");
    }

    public static <T> T getService(Class<T> type, String key) {
        return getService(type, key, true);
    }

    public static <T> T getService(Class<T> type, boolean isSingleton) {
        return getService(type, "", isSingleton);
    }

    /**
     * 如果指定了key,以key为准;如果key为空,以@SPI注解中的值为准;默认单例
     */
    public static <T> T getService(Class<T> type, String key, boolean isSingleton) {
        if (!type.isInterface()) {
            throw new IllegalArgumentException("type (" + type + ") is not an interface!");
        }
        if (!withSpiAnnotation(type)) {
            throw new IllegalArgumentException("type (" + type + ") is NOT annotated with @" + SPI.class.getSimpleName() + "!");
        }
        Map<String, Class> services = loadServices(type);
        if (CollectionUtils.isEmpty(services)) {
            return null;
        }
        Class<T> clazz;
        if (StringUtils.isNotEmpty(key)) {
            clazz = services.get(key);
        } else {
            SPI annotation = type.getAnnotation(SPI.class);
            clazz = services.get(annotation.value());
        }
        if (clazz == null) {
            return null;
        }
        if (isSingleton) {
            String name = type + "-" + key;
            Holder<Object> singleton = cachedInstances.get(name);
            if (singleton == null) {
                Holder holder = new Holder();
                holder.set(ClassUtils.instantiateClass(clazz));
                cachedInstances.putIfAbsent(name, holder);
                singleton = cachedInstances.get(name);
            }
            return (T) singleton.get();
        }
        return ClassUtils.instantiateClass(clazz);
    }

    /**
     * 加载资源文件，获取所有的指定类型的class，key ---> class
     */
    public static Map<String, Class> loadServices(Class type) {
        String dir = SERVICES_DIRECTORY + type.getName();
        Map<String, Class> services = SERVICE_CACHE.get(dir);
        if (services == null) {
            synchronized (SERVICE_CACHE) {
                if (SERVICE_CACHE.containsKey(dir)) {
                    services = SERVICE_CACHE.get(dir);
                } else {
                    services = new HashMap<>(8);
                    loadDirectory(services, dir);
                    SERVICE_CACHE.put(dir, services);
                }
            }
        }
        return services;
    }

    /**
     * 加载指定目录下资源
     */
    private static void loadDirectory(Map<String, Class> services, String dir) {
        ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
        try {
            Enumeration<URL> urls = classLoader.getResources(dir);
            if (urls == null) {
                return;
            }
            while (urls.hasMoreElements()) {
                loadResource(services, classLoader, urls.nextElement());
            }
        } catch (Exception e) {
            log.error("can not load directory {}", dir, e);
        }
    }

    /**
     * 解析资源文件
     */
    private static void loadResource(Map<String, Class> services, ClassLoader classLoader, URL url) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                // # 后面的为注释内容
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    String name = null;
                    int i = line.indexOf('=');
                    if (i > 0) {
                        name = line.substring(0, i).trim();
                        line = line.substring(i + 1).trim();
                    }
                    if (line.length() > 0) {
                        //实现类
                        Class<?> clazzName = Class.forName(line, true, classLoader);
                        services.put(name, clazzName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("load resource failure , url = {}", url, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {
                }
            }
        }
    }
}
