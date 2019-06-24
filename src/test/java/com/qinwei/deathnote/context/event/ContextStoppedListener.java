package com.qinwei.deathnote.context.event;

import com.qinwei.deathnote.context.annotation.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qinwei
 * @date 2019-06-21
 */
@Slf4j
@Component
public class ContextStoppedListener implements ApplicationListener<ContextStoppedEvent> {

    @Override
    public void onApplicationEvent(ContextStoppedEvent event) {
        log.info("ContextStoppedEvent : timestamp [ {} ]", event.getTimestamp());
    }
}
