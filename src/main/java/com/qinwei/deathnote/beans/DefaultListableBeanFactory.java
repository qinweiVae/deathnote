package com.qinwei.deathnote.beans;

import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
        implements ConfigurableListableBeanFactory {


    @Override
    public void preInstantiateSingletons() {

    }

    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        return new String[0];
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons) {
        return new String[0];
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        return null;
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons) {
        return null;
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
        return null;
    }

    @Override
    public <T extends Annotation> T findAnnotationOnBean(String beanName, Class<T> annotationType) {
        return null;
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        return null;
    }
}
