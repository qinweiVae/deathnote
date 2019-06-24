package com.qinwei.deathnote.context.event;

import com.qinwei.deathnote.context.annotation.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qinwei
 * @date 2019-06-12
 */
@Slf4j
@Component
public class ContextCloseListener implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("ContextClosedEvent : timestamp [ {} ]", event.getTimestamp());
    }
}
