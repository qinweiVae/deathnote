package com.qinwei.deathnote.beans.factory;

import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.beans.registry.SingletonBeanRegistry;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public interface ConfigurableListableBeanFactory extends ListableBeanFactory, AutowireCapableBeanFactory,
        ConfigurableBeanFactory, SingletonBeanRegistry {

    BeanDefinition getBeanDefinition(String beanName);

    void preInstantiateSingletons();
}
