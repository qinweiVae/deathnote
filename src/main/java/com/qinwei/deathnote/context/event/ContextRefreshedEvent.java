package com.qinwei.deathnote.context.event;

import com.qinwei.deathnote.context.ApplicationContext;

/**
 * @author qinwei
 * @date 2019-06-10
 */
public class ContextRefreshedEvent extends ApplicationContextEvent {

    public ContextRefreshedEvent(ApplicationContext source) {
        super(source);
    }
}
