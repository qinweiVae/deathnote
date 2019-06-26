package com.qinwei.deathnote.support.scan;

import com.qinwei.deathnote.utils.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author qinwei
 * @date 2019-05-13
 */
public class FieldAnnotationScanner extends ClasspathScanner {

    private static Map<String, Set<Field>> annotationCache = new ConcurrentHashMap<>();

    public Set<Field> scan(Class<? extends Annotation> annotation) {
        //默认获取classpath下的所有class
        return scan(annotation, "");
    }

    public Set<Field> scan(Class<? extends Annotation> annotation, String basePackage) {
        String key = annotation.getName() + "-" + basePackage;
        Set<Field> result = annotationCache.get(key);
        if (result == null) {
            synchronized (annotationCache) {
                if (annotationCache.get(key) == null) {
                    result = scanField(annotation, basePackage);
                    annotationCache.put(key, result);
                } else {
                    result = annotationCache.get(key);
                }
            }
        }
        return result;
    }

    private Set<Field> scanField(Class<? extends Annotation> annotation, String basePackage) {
        return super.scan(basePackage)
                .stream()
                .map(Class::getDeclaredFields)
                .flatMap(Arrays::stream)
                .filter(field -> AnnotationUtils.findAnnotation((AnnotatedElement) field, annotation) != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
