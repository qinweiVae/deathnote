package com.qinwei.deathnote.beans.factory;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public interface ConfigurableListableBeanFactory extends ListableBeanFactory, AutowireCapableBeanFactory,
        ConfigurableBeanFactory {

    void preInstantiateSingletons();
}
