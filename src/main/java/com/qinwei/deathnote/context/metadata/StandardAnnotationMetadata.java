package com.qinwei.deathnote.context.metadata;

import com.qinwei.deathnote.utils.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author qinwei
 * @date 2019-06-17
 */
public class StandardAnnotationMetadata extends StandardClassMetadata implements AnnotationMetadata {

    private final Annotation[] annotations;

    private final boolean nestedAnnotationsAsMap;

    public StandardAnnotationMetadata(Class<?> introspectedClass) {
        this(introspectedClass, false);
    }

    public StandardAnnotationMetadata(Class<?> introspectedClass, boolean nestedAnnotationsAsMap) {
        super(introspectedClass);
        this.annotations = introspectedClass.getAnnotations();
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
    }

    @Override
    public Set<String> getAnnotationTypes() {
        return Arrays.stream(this.annotations)
                .map(ann -> ann.annotationType().getName())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<String> getMetaAnnotationTypes(String annotationName) {
        return this.annotations.length > 0 ?
                AnnotationUtils.getMetaAnnotationTypes(getIntrospectedClass(), annotationName) :
                Collections.emptySet();
    }

    @Override
    public boolean hasAnnotation(String annotationName) {
        return Arrays.stream(this.annotations)
                .anyMatch(ann -> ann.annotationType().getName().equals(annotationName));
    }

    @Override
    public boolean hasMetaAnnotation(String metaAnnotationName) {
        return this.annotations.length > 0 &&
                AnnotationUtils.hasAnnotation(getIntrospectedClass(), metaAnnotationName);
    }

    @Override
    public boolean hasAnnotatedMethods(String annotationName) {
        try {
            return Arrays.stream(getIntrospectedClass().getDeclaredMethods())
                    .anyMatch(method -> !method.isBridge()
                            && method.getAnnotations().length > 0
                            && AnnotationUtils.hasAnnotation(method, annotationName)
                    );
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to introspect annotated methods on " + getIntrospectedClass(), ex);
        }
    }

    @Override
    public Set<MethodMetadata> getAnnotatedMethods(String annotationName) {
        try {
            return Arrays.stream(getIntrospectedClass().getDeclaredMethods())
                    .filter(method -> !method.isBridge()
                            && method.getAnnotations().length > 0
                            && AnnotationUtils.hasAnnotation(method, annotationName)
                    )
                    .map(method -> new StandardMethodMetadata(method, this.nestedAnnotationsAsMap))
                    .collect(Collectors.toCollection(() -> new LinkedHashSet<>(4)));
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to introspect annotated methods on " + getIntrospectedClass(), ex);
        }
    }

    @Override
    public boolean isAnnotated(String annotationName) {
        return this.annotations.length > 0 &&
                AnnotationUtils.hasAnnotation(getIntrospectedClass(), annotationName);
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName) {
        return this.annotations.length > 0
                ? AnnotationUtils.getAnnotationAttributes(getIntrospectedClass(), annotationName, this.nestedAnnotationsAsMap)
                : null;
    }
}
