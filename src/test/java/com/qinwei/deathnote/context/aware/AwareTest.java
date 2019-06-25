package com.qinwei.deathnote.context.aware;

import com.qinwei.deathnote.beans.factory.BeanFactory;
import com.qinwei.deathnote.config.Config;
import com.qinwei.deathnote.context.ApplicationContext;
import com.qinwei.deathnote.context.annotation.Component;
import com.qinwei.deathnote.context.event.ApplicationEventPublisher;

/**
 * @author qinwei
 * @date 2019-06-25
 */
@Component("aware")
public class AwareTest implements ApplicationContextAware, BeanFactoryAware, ConfigAware, ApplicationEventPublisherAware {

    private ApplicationContext applicationContext;

    private BeanFactory beanFactory;

    private Config config;

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }


    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public Config getConfig() {
        return config;
    }

    public ApplicationEventPublisher getApplicationEventPublisher() {
        return applicationEventPublisher;
    }
}
