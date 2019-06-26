package com.qinwei.deathnote.context.event;

import java.lang.reflect.Method;

/**
 * @author qinwei
 * @date 2019-06-26
 * todo
 */
public class ApplicationListenerMethodAdapter implements ApplicationListener<ApplicationEvent> {

    public ApplicationListenerMethodAdapter(String beanName, Class<?> targetClass, Method method) {

    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {

    }
}
