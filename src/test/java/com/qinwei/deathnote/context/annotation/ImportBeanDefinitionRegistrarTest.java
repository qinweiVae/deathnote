package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.beans.factory.BeanFactory;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.context.aware.BeanFactoryAware;
import com.qinwei.deathnote.context.metadata.AnnotationMetadata;

/**
 * @author qinwei
 * @date 2019-06-26
 */
public class ImportBeanDefinitionRegistrarTest implements ImportBeanDefinitionRegistrar, BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        System.out.println("ImportBeanDefinitionRegistrar --- " + importingClassMetadata.getClassName());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}
