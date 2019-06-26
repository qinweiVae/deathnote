package com.qinwei.deathnote.support.scan;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author qinwei
 * @date 2019-05-14
 */
public class SubTypeScanner extends ClasspathScanner {

    private static Map<String, Set<Class>> subtypeCache = new ConcurrentHashMap<>();

    public Set<Class> scan(Class clazz) {
        //默认获取classpath下的所有class
        return scan(clazz, "");
    }

    public Set<Class> scan(Class<?> clazz, String basePackage) {
        String key = clazz.getName() + "-" + basePackage;
        Set<Class> result = subtypeCache.get(key);
        if (result == null) {
            synchronized (subtypeCache) {
                if (subtypeCache.get(key) == null) {
                    result = scanSubType(clazz, basePackage);
                    subtypeCache.put(key, result);
                } else {
                    result = subtypeCache.get(key);
                }
            }
        }
        return result;
    }

    private Set<Class> scanSubType(Class<?> cls, String basePackage) {
        return super.scan(basePackage)
                .stream()
                .filter(clazz -> cls.isAssignableFrom(clazz) && cls != clazz)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
