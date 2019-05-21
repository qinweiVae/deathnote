package com.qinwei.deathnote.beans;

/**
 * @author qinwei
 * @date 2019-05-21
 */
public interface BeanFactory {

    Object getBean(String name);

    <T> T getBean(String name, Class<T> requiredType);

    <T> T getBean(Class<T> requiredType);

    String[] getAliases(String name);

    boolean containsBean(String name);
}
