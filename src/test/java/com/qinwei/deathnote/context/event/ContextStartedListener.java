package com.qinwei.deathnote.context.event;

import com.qinwei.deathnote.context.annotation.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qinwei
 * @date 2019-06-21
 */
@Slf4j
@Component
public class ContextStartedListener implements ApplicationListener<ContextStartedEvent> {

    @Override
    public void onApplicationEvent(ContextStartedEvent event) {
        log.info("ContextStartedEvent : timestamp [ {} ]", event.getTimestamp());
    }
}
