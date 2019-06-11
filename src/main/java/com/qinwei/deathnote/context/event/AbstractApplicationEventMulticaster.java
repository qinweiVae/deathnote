package com.qinwei.deathnote.context.event;

import com.qinwei.deathnote.beans.factory.BeanFactory;
import com.qinwei.deathnote.context.annotation.AnnotationOrderComparator;
import com.qinwei.deathnote.context.aware.BeanFactoryAware;
import com.qinwei.deathnote.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-11
 */
public abstract class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster, BeanFactoryAware {

    private BeanFactory beanFactory;

    private final Object mutex = new Object();

    private final ListenerRetriever listenerRetriever = new ListenerRetriever();

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        synchronized (mutex) {
            //todo 如果是代理类的话需要先移除掉，不然会调用2次
            //applicationListeners.add(listener);
        }
    }

    @Override
    public void removeApplicationListener(ApplicationListener<?> listener) {
        synchronized (mutex) {
            //applicationListeners.remove(listener);
        }
    }

    @Override
    public void addApplicationListenerBean(String listenerBeanName) {
        synchronized (mutex) {
            //applicationListenerBeans.add(listenerBeanName);
        }
    }

    @Override
    public void removeApplicationListenerBean(String listenerBeanName) {
        synchronized (mutex) {
            //applicationListenerBeans.remove(listenerBeanName);
        }
    }

    @Override
    public void removeAllListeners() {
        synchronized (mutex) {
            //applicationListeners.clear();
            //applicationListenerBeans.clear();
        }
    }

    protected Collection<ApplicationListener<?>> getApplicationListeners() {
        return null;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    private BeanFactory getBeanFactory() {
        if (this.beanFactory == null) {
            throw new IllegalStateException("ApplicationEventMulticaster cannot retrieve listener beans , because it is not associated with a BeanFactory");
        }
        return this.beanFactory;
    }

    private class ListenerRetriever {

        public final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();

        public final Set<String> applicationListenerBeans = new LinkedHashSet<>();

        protected Collection<ApplicationListener<?>> getApplicationListeners() {
            List<ApplicationListener<?>> allListeners = new ArrayList<>(this.applicationListeners.size() + this.applicationListenerBeans.size());
            allListeners.addAll(this.applicationListeners);
            if (CollectionUtils.isNotEmpty(this.applicationListenerBeans)) {
                BeanFactory beanFactory = getBeanFactory();
                for (String beanName : this.applicationListenerBeans) {
                    ApplicationListener listener = beanFactory.getBean(beanName, ApplicationListener.class);
                    if (!allListeners.contains(listener)) {
                        allListeners.add(listener);
                    }
                }
            }
            AnnotationOrderComparator.sort(allListeners);
            return allListeners;
        }
    }
}
