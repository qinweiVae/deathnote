package com.qinwei.deathnote.beans.postprocessor;

import com.qinwei.deathnote.aop.support.AopUtils;
import com.qinwei.deathnote.beans.bean.SmartInitializingSingleton;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.context.ApplicationContext;
import com.qinwei.deathnote.context.annotation.AnnotationOrderComparator;
import com.qinwei.deathnote.context.annotation.EventListener;
import com.qinwei.deathnote.context.annotation.ScopedProxyUtils;
import com.qinwei.deathnote.context.aware.ApplicationContextAware;
import com.qinwei.deathnote.context.event.ApplicationListener;
import com.qinwei.deathnote.context.event.ApplicationListenerMethodAdapter;
import com.qinwei.deathnote.context.event.EventListenerFactory;
import com.qinwei.deathnote.utils.AnnotationUtils;
import com.qinwei.deathnote.utils.CollectionUtils;
import com.qinwei.deathnote.utils.MethodIntrospector;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-06-18
 */
public class EventListenerMethodProcessor implements SmartInitializingSingleton, ApplicationContextAware, BeanFactoryPostProcessor {

    private ApplicationContext applicationContext;

    private ConfigurableListableBeanFactory beanFactory;

    private List<EventListenerFactory> eventListenerFactories;

    @Override
    public void afterSingletonsInstantiated() {
        String[] beanNames = this.beanFactory.getBeanNamesForType(Object.class);
        for (String name : beanNames) {
            // 如果 bean 是 ScopedProxyMode.TARGET_CLASS，且是原始的bean对象则跳过，只对 代理bean 操作，不然就重复处理了
            if (ScopedProxyUtils.isScopedTarget(name)) {
                continue;
            }
            Class<?> targetClass = AopUtils.determineTargetClass(this.beanFactory, name);
            if (targetClass != null) {
                processBean(name, targetClass);
            }
        }
    }

    private void processBean(final String beanName, final Class<?> targetType) {
        Map<Method, EventListener> annotatedMethods =
                MethodIntrospector.selectMethods(targetType, method -> AnnotationUtils.findAnnotation(method, EventListener.class));

        if (CollectionUtils.isNotEmpty(annotatedMethods)) {
            for (Method method : annotatedMethods.keySet()) {
                for (EventListenerFactory factory : this.eventListenerFactories) {
                    if (factory.supportsMethod(method)) {
                        Method methodToUse = MethodIntrospector.selectInvocableMethod(method, this.applicationContext.getType(beanName));
                        ApplicationListener<?> applicationListener = factory.createApplicationListener(beanName, targetType, methodToUse);
                        if (applicationListener instanceof ApplicationListenerMethodAdapter) {
                            ((ApplicationListenerMethodAdapter) applicationListener).init(this.applicationContext);
                        }
                        this.applicationContext.addApplicationListener(applicationListener);
                        break;
                    }
                }
            }
        }
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
