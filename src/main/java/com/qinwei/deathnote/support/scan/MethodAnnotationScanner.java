package com.qinwei.deathnote.support.scan;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinwei
 * @date 2019-05-13
 */
public class MethodAnnotationScanner extends ClasspathScanner {

    private static Map<Class<? extends Annotation>, Set<Method>> annotationCache = new ConcurrentHashMap<>();

    public Set<Method> scan(Class<? extends Annotation> annotation) {
        //默认获取classpath下的所有class
        return scan(annotation, "");
    }

    public Set<Method> scan(Class<? extends Annotation> annotation, String basePackage) {
        Set<Method> result = annotationCache.get(annotation);
        if (result == null) {
            synchronized (annotationCache) {
                if (annotationCache.containsKey(annotation)) {
                    result = annotationCache.get(annotation);
                } else {
                    Set<Class> classes = super.scan(basePackage);
                    result = new LinkedHashSet<>();
                    for (Class clazz : classes) {
                        Method[] methods = clazz.getDeclaredMethods();
                        for (Method method : methods) {
                            if (method.isAnnotationPresent(annotation)) {
                                result.add(method);
                            }
                        }
                    }
                    annotationCache.put(annotation, result);
                }
            }
        }
        return result;
    }
}