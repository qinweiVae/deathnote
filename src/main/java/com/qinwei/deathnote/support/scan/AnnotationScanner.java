package com.qinwei.deathnote.support.scan;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinwei
 * @date 2019-05-12 21:57
 */
public class AnnotationScanner extends ClasspathScanner {

    private static Map<Class<? extends Annotation>, List<Class>> annotationCache = new ConcurrentHashMap<>();

    public List<Class> scan(Class<? extends Annotation> annotation) {
        List<Class> result = annotationCache.get(annotation);
        if (result == null) {
            synchronized (annotationCache) {
                if (annotationCache.containsKey(annotation)) {
                    result = annotationCache.get(annotation);
                } else {
                    //获取classpath下的所有class
                    Set<Class> classes = scan("");
                    result = new ArrayList<>();
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
