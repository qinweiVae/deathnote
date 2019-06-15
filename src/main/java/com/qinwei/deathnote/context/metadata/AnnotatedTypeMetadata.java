package com.qinwei.deathnote.context.metadata;

import java.util.Map;

/**
 * @author qinwei
 * @date 2019-06-14
 */
public interface AnnotatedTypeMetadata {

    boolean isAnnotated(String annotationName);

    Map<String, Object> getAnnotationAttributes(String annotationName);


}
