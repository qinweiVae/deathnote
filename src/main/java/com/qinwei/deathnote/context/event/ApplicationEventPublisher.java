package com.qinwei.deathnote.context.event;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public interface ApplicationEventPublisher {

    void publishEvent(Object event);
}
