package com.qinwei.deathnote.support.scan;

import com.qinwei.deathnote.utils.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author qinwei
 * @date 2019-05-12 21:57
 */
public class TypeAnnotationScanner extends ClasspathScanner {

    private static Map<String, Set<Class>> annotationCache = new ConcurrentHashMap<>();

    public Set<Class> scan(Class<? extends Annotation> annotation) {
        //默认获取classpath下的所有class
        return scan(annotation, "");
    }

    public Set<Class> scan(Class<? extends Annotation> annotation, String basePackage) {
        String key = annotation.getName() + "-" + basePackage;
        Set<Class> result = annotationCache.get(key);
        if (result == null) {
            synchronized (annotationCache) {
                if (annotationCache.get(key) == null) {
                    result = scanType(annotation, basePackage);
                    annotationCache.put(key, result);
                } else {
                    result = annotationCache.get(key);
                }
            }
        }
        return result;
    }

    private Set<Class> scanType(Class<? extends Annotation> annotation, String basePackage) {
        return super.scan(basePackage)
                .stream()
                .filter(clazz -> AnnotationUtils.findAnnotation(clazz, annotation) != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
