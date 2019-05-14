package com.qinwei.deathnote.support.scan;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    public Set<Class> scan(Class<? extends Annotation> cls, String basePackage) {
        Set<Class> result = subtypeCache.get(cls);
        if (result == null) {
            synchronized (subtypeCache) {
                if (subtypeCache.containsKey(cls)) {
                    result = subtypeCache.get(cls);
                } else {
                    Set<Class> classes = super.scan(basePackage);
                    result = new LinkedHashSet<>();
                    for (Class clazz : classes) {
                        if (cls.isAssignableFrom(clazz) && cls != clazz) {
                            result.add(clazz);
                        }
                    }
                    subtypeCache.put(cls + "-" + basePackage, result);
                }
            }
        }
        return result;
    }

}
