package com.qinwei.deathnote.beans.alias;

/**
 * @author qinwei
 * @date 2019-05-23
 */
public interface SingletonBeanRegistry {

    void registerSingleton(String beanName, Object singletonObject);

    Object getSingleton(String beanName);

    boolean containsSingleton(String beanName);

    String[] getSingletonNames();

    int getSingletonCount();
}
