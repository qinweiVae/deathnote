package com.qinwei.deathnote.event;

import com.qinwei.deathnote.context.event.ApplicationListener;
import com.qinwei.deathnote.context.event.ContextClosedEvent;

/**
 * @author qinwei
 * @date 2019-06-12
 */
public class ContextCloseListener implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        System.out.println(event.getTimestamp());
    }
}
