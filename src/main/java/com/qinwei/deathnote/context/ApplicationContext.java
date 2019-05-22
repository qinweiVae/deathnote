package com.qinwei.deathnote.context;

import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.config.conf.ConfigFactory;
import com.qinwei.deathnote.context.event.ApplicationEventPublisher;
import com.qinwei.deathnote.context.event.ApplicationListener;

/**
 * @author qinwei
 * @date 2019-05-21
 */
public interface ApplicationContext extends ConfigFactory, Lifecycle, ApplicationEventPublisher {

    void addApplicationListener(ApplicationListener<?> listener);

    void refresh();

    void registerShutdownHook();

    ConfigurableListableBeanFactory getBeanFactory();
}
