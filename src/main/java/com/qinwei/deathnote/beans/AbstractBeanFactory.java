package com.qinwei.deathnote.beans;

import com.qinwei.deathnote.beans.extension.BeanPostProcessor;
import com.qinwei.deathnote.beans.factory.ConfigurableBeanFactory;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public abstract class AbstractBeanFactory implements ConfigurableBeanFactory {

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {

    }

    @Override
    public void destroyBean(String beanName, Object beanInstance) {

    }

    @Override
    public void destroySingletons() {

    }

    @Override
    public Object getBean(String name) {
        return null;
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) {
        return null;
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        return null;
    }

    @Override
    public String[] getAliases(String name) {
        return new String[0];
    }

    @Override
    public boolean containsBean(String name) {
        return false;
    }
}
