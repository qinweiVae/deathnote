package com.qinwei.deathnote.context;

import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.beans.factory.ListableBeanFactory;
import com.qinwei.deathnote.beans.postprocessor.BeanFactoryPostProcessor;
import com.qinwei.deathnote.config.ConfigFactory;
import com.qinwei.deathnote.context.event.ApplicationEventPublisher;
import com.qinwei.deathnote.context.event.ApplicationListener;
import com.qinwei.deathnote.context.lifecycle.Lifecycle;

import java.io.Closeable;

/**
 * @author qinwei
 * @date 2019-05-21
 */
public interface ApplicationContext extends ListableBeanFactory, ConfigFactory, Lifecycle, ApplicationEventPublisher, Closeable {

    String CONFIG = "config";

    void addApplicationListener(ApplicationListener<?> listener);

    void refresh();

    void registerShutdownHook();

    ConfigurableListableBeanFactory getBeanFactory();

    void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor);

    void close();
}
