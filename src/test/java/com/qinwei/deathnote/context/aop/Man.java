package com.qinwei.deathnote.context.aop;

import com.qinwei.deathnote.aop.support.AopContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qinwei
 * @date 2019-07-23
 */
//@Component
//@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
public class Man implements People {

    @Override
    public String work() {
        log.info("man work ... proxy : {}", AopContext.currentProxy().getClass());
        /*if (1 == 1) {
            throw new RuntimeException("test");
        }*/
        return "qinwei";
    }

}
