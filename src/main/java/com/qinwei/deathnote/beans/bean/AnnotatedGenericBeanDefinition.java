package com.qinwei.deathnote.beans.bean;

import com.qinwei.deathnote.context.metadata.AnnotationMetadata;
import com.qinwei.deathnote.context.metadata.StandardAnnotationMetadata;

/**
 * @author qinwei
 * @date 2019-06-17
 */
public class AnnotatedGenericBeanDefinition extends AbstractBeanDefinition implements AnnotatedBeanDefinition {

    private AnnotationMetadata metadata;

    public AnnotatedGenericBeanDefinition(Class<?> beanClass) {
        setBeanClass(beanClass);
        this.metadata = new StandardAnnotationMetadata(beanClass, true);
    }

    public AnnotatedGenericBeanDefinition(AnnotationMetadata metadata) {
        if (metadata instanceof StandardAnnotationMetadata) {
            setBeanClass(((StandardAnnotationMetadata) metadata).getIntrospectedClass());
        } else {
            setBeanClassName(metadata.getClassName());
        }
        this.metadata = metadata;
    }

    public AnnotatedGenericBeanDefinition(AbstractBeanDefinition beanDefinition) {
        copyFrom(beanDefinition);
    }

    public void copyFrom(AbstractBeanDefinition beanDefinition) {
        setBeanClassName(beanDefinition.getBeanClassName());
        setFactoryBeanName(beanDefinition.getFactoryBeanName());
        setFactoryMethod(beanDefinition.getFactoryMethod());
        setScope(beanDefinition.getScope());
        setAbstract(beanDefinition.isAbstract());
        if (beanDefinition.hasBeanClass()) {
            setBeanClass(beanDefinition.getBeanClass());
        }
        Boolean lazyInit = beanDefinition.getLazyInit();
        if (lazyInit != null) {
            setLazyInit(lazyInit);
        }
        setDependsOn(beanDefinition.getDependsOn());
        setPrimary(beanDefinition.isPrimary());
        setInitMethodName(beanDefinition.getInitMethodName());
        setDestroyMethodName(beanDefinition.getDestroyMethodName());
    }

    @Override
    protected AbstractBeanDefinition cloneBeanDefinition() {
        return new AnnotatedGenericBeanDefinition(this);
    }

    @Override
    public AnnotationMetadata getMetadata() {
        return this.metadata;
    }
}
