package com.qinwei.deathnote.context.aware;

import com.qinwei.deathnote.context.event.ApplicationEventPublisher;

/**
 * @author qinwei
 * @date 2019-06-10
 */
public interface ApplicationEventPublisherAware extends Aware{

    void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher);
}
