package com.qinwei.deathnote.beans.registry;

import com.qinwei.deathnote.beans.bean.BeanDefinition;

/**
 * @author qinwei
 * @date 2019-05-25 15:08
 */
public interface BeanDefinitionRegistry extends AliasRegistry {

    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);

    void removeBeanDefinition(String beanName);

    BeanDefinition getBeanDefinition(String beanName);

    boolean containsBeanDefinition(String beanName);

    String[] getBeanDefinitionNames();

    int getBeanDefinitionCount();
}
