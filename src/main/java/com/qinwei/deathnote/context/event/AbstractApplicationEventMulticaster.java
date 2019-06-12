package com.qinwei.deathnote.context.event;

import com.qinwei.deathnote.beans.factory.BeanFactory;
import com.qinwei.deathnote.context.annotation.AnnotationOrderComparator;
import com.qinwei.deathnote.context.aware.BeanFactoryAware;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author qinwei
 * @date 2019-06-11
 */
public abstract class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster, BeanFactoryAware {

    private BeanFactory beanFactory;

    private final Object mutex = new Object();

    public final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();

    public final Set<String> applicationListenerBeans = new LinkedHashSet<>();

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        synchronized (mutex) {
            //todo 如果是代理类的话需要先移除掉，不然会调用2次
            applicationListeners.add(listener);
        }
    }

    @Override
    public void removeApplicationListener(ApplicationListener<?> listener) {
        synchronized (mutex) {
            applicationListeners.remove(listener);
        }
    }

    @Override
    public void addApplicationListenerBean(String listenerBeanName) {
        synchronized (mutex) {
            applicationListenerBeans.add(listenerBeanName);
        }
    }

    @Override
    public void removeApplicationListenerBean(String listenerBeanName) {
        synchronized (mutex) {
            applicationListenerBeans.remove(listenerBeanName);
        }
    }

    @Override
    public void removeAllListeners() {
        synchronized (mutex) {
            applicationListeners.clear();
            applicationListenerBeans.clear();
        }
    }

    protected Collection<ApplicationListener<?>> getApplicationListeners() {
        synchronized (mutex) {
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

    /**
     * 获取ApplicationEvent 对应的 ApplicationListeners
     */
    protected Collection<ApplicationListener<?>> getApplicationListeners(ApplicationEvent event) {
        if (event == null) {
            return new ArrayList<>();
        }
        return getApplicationListeners().stream()
                .filter(listener -> supportEvent(listener, event))
                .collect(Collectors.toList());
    }

    private boolean supportEvent(ApplicationListener<?> listener, ApplicationEvent event) {
        Class genericType = ClassUtils.findGenericType(listener.getClass(), 0);
        if (genericType == null) {
            return false;
        }
        return ClassUtils.isAssignable(genericType, event.getClass());
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

}
