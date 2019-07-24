package com.qinwei.deathnote.beans.bean;

/**
 * @author qinwei
 * @date 2019-07-23
 */
public interface FactoryBean<T> {

    T getObject() throws Exception;

    Class<?> getObjectType();

    default boolean isSingleton() {
        return true;
    }
}
