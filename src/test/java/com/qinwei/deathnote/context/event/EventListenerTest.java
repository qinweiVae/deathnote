package com.qinwei.deathnote.context.event;

import com.qinwei.deathnote.beans.bean.Domain1;
import com.qinwei.deathnote.beans.bean.Domain2;
import com.qinwei.deathnote.context.annotation.Component;
import com.qinwei.deathnote.context.annotation.EventListener;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qinwei
 * @date 2019-07-29
 */
@Component
@Slf4j
public class EventListenerTest {


    /*@EventListener(classes = {Domain1.class, Domain2.class})
    public void eventTest() {
        log.info("event listener ......");
    }*/

   /* @EventListener(classes = {Domain1.class})
    public Domain2 eventTest() {
        log.info("event listener ......");
        Domain2 domain2 = new Domain2();
        domain2.setBrand("evenListener");
        return domain2;
    }*/

   /* @EventListener()
    public void eventTest(Domain1 domain1) {
        log.info("event listener ......");
    }*/

    /*@EventListener()
    public void eventTest(Domain1 domain1, Domain2 domain2) {
        log.info("event listener ......");
    }*/

    @EventListener
    public Domain2 eventTest(Domain1 domain1) {
        log.info("@EventListener ...... ");
        Domain2 domain2 = new Domain2();
        domain2.setBrand("evenListener");
        return domain2;
    }
}
