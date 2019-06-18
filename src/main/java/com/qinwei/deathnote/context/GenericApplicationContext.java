package com.qinwei.deathnote.context;

import com.qinwei.deathnote.beans.DefaultListableBeanFactory;
import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author qinwei
 * @date 2019-06-18
 */
public class GenericApplicationContext extends AbstractApplicationContext implements BeanDefinitionRegistry {

    private final DefaultListableBeanFactory beanFactory;

    private final AtomicBoolean refreshed = new AtomicBoolean();

    public GenericApplicationContext() {
        this.beanFactory = new DefaultListableBeanFactory();
    }

    @Override
    public ConfigurableListableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    @Override
    protected void refreshBeanFactory() {
        if (!this.refreshed.compareAndSet(false, true)) {
            throw new IllegalStateException("GenericApplicationContext does not support multiple refresh attempts: just call 'refresh' once");
        }
    }

    // ------------------------------------------------------------------------------

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        this.beanFactory.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public void removeBeanDefinition(String beanName) {
        this.beanFactory.removeBeanDefinition(beanName);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return this.beanFactory.getBeanDefinition(beanName);
    }

    @Override
    public void registerAlias(String name, String alias) {
        this.beanFactory.registerAlias(name, alias);
    }

    @Override
    public void removeAlias(String alias) {
        this.beanFactory.removeAlias(alias);
    }

    @Override
    public boolean isAlias(String name) {
        return this.beanFactory.isAlias(name);
    }

}
