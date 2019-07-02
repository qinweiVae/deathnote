package com.qinwei.deathnote.aop;

import com.qinwei.deathnote.aop.annotation.AspectMetadata;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;

/**
 * @author qinwei
 * @date 2019-07-02
 */
public class BeanFactoryAspectInstanceFactory implements AspectInstanceFactory {

    private final ConfigurableListableBeanFactory beanFactory;

    private final String beanName;

    private final AspectMetadata aspectMetadata;

    public BeanFactoryAspectInstanceFactory(ConfigurableListableBeanFactory beanFactory, String beanName, AspectMetadata metadata) {
        this.beanFactory = beanFactory;
        this.beanName = beanName;
        this.aspectMetadata = metadata;
    }

    @Override
    public Object getAspectInstance() {
        return this.beanFactory.getBean(this.beanName);
    }

    @Override
    public AspectMetadata getAspectMetadata() {
        return this.aspectMetadata;
    }
}
