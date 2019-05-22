package com.qinwei.deathnote.context.event;

import java.util.EventListener;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

    void onApplicationEvent(E event);
}
