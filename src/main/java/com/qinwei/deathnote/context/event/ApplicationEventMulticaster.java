package com.qinwei.deathnote.context.event;

/**
 * @author qinwei
 * @date 2019-06-11
 */
public interface ApplicationEventMulticaster {

    void addApplicationListener(ApplicationListener<?> listener);

    void removeApplicationListener(ApplicationListener<?> listener);

    void addApplicationListenerBean(String listenerBeanName);

    void removeApplicationListenerBean(String listenerBeanName);

    void removeAllListeners();

    void multicastEvent(ApplicationEvent event);
}
