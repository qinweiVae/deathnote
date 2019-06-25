package com.qinwei.deathnote.support.scan;

import com.qinwei.deathnote.utils.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
        Set<Field> result = annotationCache.get(annotation);
        if (result == null) {
            synchronized (annotationCache) {
                if (annotationCache.containsKey(annotation)) {
                    result = annotationCache.get(annotation);
                } else {
                    Set<Class> classes = super.scan(basePackage);
                    result = new LinkedHashSet<>();
                    for (Class clazz : classes) {
                        Field[] fields = clazz.getDeclaredFields();
                        for (Field field : fields) {
                            if (AnnotationUtils.findAnnotation((AnnotatedElement) field, annotation) != null) {
                                result.add(field);
                            }
                        }
                    }
                    annotationCache.put(annotation.getSimpleName() + "-" + basePackage, result);
                }
            }
        }
        return result;
    }
}
