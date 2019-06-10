package com.qinwei.deathnote.context.aware;

import com.qinwei.deathnote.context.ApplicationContext;

/**
 * @author qinwei
 * @date 2019-06-10
 */
public interface ApplicationContextAware extends Aware {

    void setApplicationContext(ApplicationContext applicationContext);
}
