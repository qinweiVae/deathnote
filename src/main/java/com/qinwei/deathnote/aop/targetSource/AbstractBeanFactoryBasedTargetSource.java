package com.qinwei.deathnote.aop.targetSource;

import com.qinwei.deathnote.beans.factory.BeanFactory;
import com.qinwei.deathnote.context.aware.BeanFactoryAware;

/**
 * @author qinwei
 * @date 2019-07-23
 */
public abstract class AbstractBeanFactoryBasedTargetSource implements TargetSource, BeanFactoryAware {

    private String targetBeanName;

    private volatile Class<?> targetClass;

    private BeanFactory beanFactory;

    public void setTargetBeanName(String targetBeanName) {
        this.targetBeanName = targetBeanName;
    }

    public String getTargetBeanName() {
        return this.targetBeanName;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        assert this.targetBeanName != null : "Property 'targetBeanName' is required";
        this.beanFactory = beanFactory;
    }

    public BeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    @Override
    public Class<?> getTargetClass() {
        Class<?> targetClass = this.targetClass;
        if (targetClass != null) {
            return targetClass;
        }
        synchronized (this) {
            targetClass = this.targetClass;
            if (targetClass == null && this.beanFactory != null) {
                targetClass = this.beanFactory.getType(this.targetBeanName);
                if (targetClass == null) {
                    Object beanInstance = this.beanFactory.getBean(this.targetBeanName);
                    targetClass = beanInstance.getClass();
                }
                this.targetClass = targetClass;
            }
            return targetClass;
        }
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public void releaseTarget(Object target) throws Exception {

    }

}
