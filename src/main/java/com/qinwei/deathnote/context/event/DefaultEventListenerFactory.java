package com.qinwei.deathnote.context.event;

import java.lang.reflect.Method;

/**
 * @author qinwei
 * @date 2019-06-18
 */
public class DefaultEventListenerFactory implements EventListenerFactory {

    @Override
    public boolean supportsMethod(Method method) {
        return true;
    }

    @Override
    public ApplicationListener<?> createApplicationListener(String beanName, Class<?> type, Method method) {
        return new ApplicationListenerMethodAdapter(beanName, type, method);
    }
}
