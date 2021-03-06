package com.qinwei.deathnote.beans.factory;

/**
 * @author qinwei
 * @date 2019-05-21
 */
public interface BeanFactory {

    String FACTORY_BEAN_NAME = "&";

    Object getBean(String name);

    <T> T getBean(String name, Class<T> requiredType);

    <T> T getBean(Class<T> requiredType);

    String[] getAliases(String name);

    boolean containsBean(String name);

    boolean isTypeMatch(String name, Class<?> typeToMatch);

    Class<?> getType(String name);

    boolean isSingleton(String name);

    boolean isPrototype(String name);
}
