package com.qinwei.deathnote.beans;

import com.qinwei.deathnote.beans.factory.AutowireCapableBeanFactory;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {

    @Override
    public <T> T createBean(Class<T> beanClass) {
        return null;
    }

    @Override
    public void autowireBean(Object existingBean) {

    }

    @Override
    public Object createBean(Class<?> beanClass, int autowireMode) {
        return null;
    }

    @Override
    public Object initializeBean(Object existingBean, String beanName) {
        return null;
    }

    @Override
    public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) {
        return null;
    }

    @Override
    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) {
        return null;
    }
}
