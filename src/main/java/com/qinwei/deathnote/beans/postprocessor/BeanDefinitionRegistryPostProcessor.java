package com.qinwei.deathnote.beans.postprocessor;

import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;

/**
 * @author qinwei
 * @date 2019-06-13
 */
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {

    void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry);
}
