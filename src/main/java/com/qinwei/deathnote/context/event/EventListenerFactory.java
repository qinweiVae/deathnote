package com.qinwei.deathnote.context.event;

import java.lang.reflect.Method;

/**
 * @author qinwei
 * @date 2019-06-26
 */
public interface EventListenerFactory {

    boolean supportsMethod(Method method);

    ApplicationListener<?> createApplicationListener(String beanName, Class<?> type, Method method);
}
