package com.qinwei.deathnote.support.scan;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinwei
 * @date 2019-05-12 21:57
 */
public class TypeAnnotationScanner extends ClasspathScanner {

    private static Map<Class<? extends Annotation>, Set<Class>> annotationCache = new ConcurrentHashMap<>();

    public Set<Class> scan(Class<? extends Annotation> annotation) {
        //默认获取classpath下的所有class
        return scan(annotation, "");
    }

    public Set<Class> scan(Class<? extends Annotation> annotation, String basePackage) {
        Set<Class> result = annotationCache.get(annotation);
        if (result == null) {
            synchronized (annotationCache) {
                if (annotationCache.containsKey(annotation)) {
                    result = annotationCache.get(annotation);
                } else {
                    Set<Class> classes = super.scan(basePackage);
                    result = new LinkedHashSet<>();
                    for (Class clazz : classes) {
                        if (clazz.isAnnotationPresent(annotation)) {
                            result.add(clazz);
                        }
                    }
                    annotationCache.put(annotation, result);
                }
            }
        }
        return result;
    }

}