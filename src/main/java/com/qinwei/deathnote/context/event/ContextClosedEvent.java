package com.qinwei.deathnote.context.event;

import com.qinwei.deathnote.context.ApplicationContext;

/**
 * @author qinwei
 * @date 2019-06-10
 */
public class ContextClosedEvent extends ApplicationContextEvent {

    public ContextClosedEvent(ApplicationContext source) {
        super(source);
    }
}
