package com.qinwei.deathnote.context.event;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public interface ApplicationEventPublisher {

    default void publishEvent(ApplicationEvent event) {
        publishEvent((Object) event);
    }

    void publishEvent(Object event);
}
