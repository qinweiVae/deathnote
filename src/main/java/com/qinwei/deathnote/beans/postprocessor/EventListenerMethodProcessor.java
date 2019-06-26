package com.qinwei.deathnote.beans.postprocessor;

import com.qinwei.deathnote.beans.bean.SmartInitializingSingleton;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.context.ApplicationContext;
import com.qinwei.deathnote.context.annotation.AnnotationOrderComparator;
import com.qinwei.deathnote.context.aware.ApplicationContextAware;
import com.qinwei.deathnote.context.event.EventListenerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-06-18
 * todo
 */
public class EventListenerMethodProcessor implements SmartInitializingSingleton, ApplicationContextAware, BeanFactoryPostProcessor {

    private ApplicationContext applicationContext;

    private ConfigurableListableBeanFactory beanFactory;

    private List<EventListenerFactory> eventListenerFactories;

    @Override
    public void afterSingletonsInstantiated() {

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;

        Map<String, EventListenerFactory> beans = beanFactory.getBeansOfType(EventListenerFactory.class, false);
        List<EventListenerFactory> factories = new ArrayList<>(beans.values());
        AnnotationOrderComparator.sort(factories);
        this.eventListenerFactories = factories;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
