package com.qinwei.deathnote.context.event;

import com.qinwei.deathnote.beans.bean.Domain2;
import com.qinwei.deathnote.context.annotation.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qinwei
 * @date 2019-06-25
 */
@Component
@Slf4j
public class PayLoadListener2 implements ApplicationListener<PayloadApplicationEvent<Domain2>> {

    @Override
    public void onApplicationEvent(PayloadApplicationEvent<Domain2> event) {
        log.info("收到通知 ... {} ", event.getPayload().getBrand());
    }
}
