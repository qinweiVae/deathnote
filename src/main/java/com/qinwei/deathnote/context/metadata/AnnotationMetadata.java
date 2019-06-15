package com.qinwei.deathnote.context.metadata;

import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-14
 */
public interface AnnotationMetadata extends ClassMetadata, AnnotatedTypeMetadata {

    Set<String> getAnnotationTypes();

    Set<String> getMetaAnnotationTypes(String annotationName);

    boolean hasAnnotation(String annotationName);

    boolean hasMetaAnnotation(String metaAnnotationName);

    boolean hasAnnotatedMethods(String annotationName);

    Set<MethodMetadata> getAnnotatedMethods(String annotationName);

}
