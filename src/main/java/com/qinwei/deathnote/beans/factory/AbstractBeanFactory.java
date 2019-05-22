package com.qinwei.deathnote.beans.factory;

import com.qinwei.deathnote.beans.extension.BeanPostProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qinwei
 * @date 2019-05-21
 */
public class AbstractBeanFactory implements ConfigurableBeanFactory {

    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        if (beanPostProcessor == null) {
            return;
        }
        this.beanPostProcessors.remove(beanPostProcessor);
        beanPostProcessors.add(beanPostProcessor);
    }

    @Override
    public void destroyBean(String beanName, Object beanInstance) {

    }

    @Override
    public void destroySingletons() {

    }

    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
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
