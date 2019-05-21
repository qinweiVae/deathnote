package com.qinwei.deathnote.beans;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-05-21
 */
public interface ListableBeanFactory extends BeanFactory {

    String[] getBeanNamesForType(Class<?> type);

    String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons);

    <T> Map<String, T> getBeansOfType(Class<T> type);

    <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons);

    Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType);

    <T extends Annotation> T findAnnotationOnBean(String beanName, Class<T> annotationType);
}
