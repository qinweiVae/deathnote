package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.context.metadata.AnnotationMetadata;

/**
 * @author qinwei
 * @date 2019-06-20
 */
public interface ImportBeanDefinitionRegistrar {

    /**
     * 注册 BeanDefinition
     */
    void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry);
}
