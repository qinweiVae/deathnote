package com.qinwei.deathnote.context.event;

import com.qinwei.deathnote.context.ApplicationContext;

/**
 * @author qinwei
 * @date 2019-06-10
 */
public abstract class ApplicationContextEvent extends ApplicationEvent {

    public ApplicationContextEvent(ApplicationContext source) {
        super(source);
    }

    public final ApplicationContext getApplicationContext() {
        return (ApplicationContext) getSource();
    }
}
