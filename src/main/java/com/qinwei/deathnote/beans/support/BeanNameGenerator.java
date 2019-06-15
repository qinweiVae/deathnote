package com.qinwei.deathnote.beans.support;

import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;

/**
 * @author qinwei
 * @date 2019-06-13
 */
public interface BeanNameGenerator {

    String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry);
}
