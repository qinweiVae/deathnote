package com.qinwei.deathnote.context.bean;

import com.qinwei.deathnote.beans.bean.InitializingBean;
import com.qinwei.deathnote.context.annotation.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qinwei
 * @date 2019-06-25
 */
@Component
@Slf4j
public class InitializingBeanTest implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        log.info(this.getClass().getName() + " : afterPropertiesSet()");
    }
}
