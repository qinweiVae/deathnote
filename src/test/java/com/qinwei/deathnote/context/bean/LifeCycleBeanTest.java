package com.qinwei.deathnote.context.bean;

import com.qinwei.deathnote.beans.bean.DisposableBean;
import com.qinwei.deathnote.beans.bean.InitializingBean;
import com.qinwei.deathnote.context.annotation.Order;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qinwei
 * @date 2019-06-25
 */
//@Component("lifeCycle")
@Order(value = 2)
@Slf4j
public class LifeCycleBeanTest implements InitializingBean, DisposableBean {

    @Override
    public void afterPropertiesSet() {
        log.info(" {} : afterPropertiesSet()", this.getClass().getName());
    }


    @Override
    public void destroy() {
        log.info("{} : destroy()", this.getClass().getName());
    }

    public void init() {
        log.info("{} : init()", this.getClass().getName());
    }

    public void close() {
        log.info("{} : close()", this.getClass().getName());
    }
}
