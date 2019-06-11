package com.qinwei.deathnote.context.aware;

/**
 * @author qinwei
 * @date 2019-06-11
 */
public interface BeanNameAware extends Aware {

    void setBeanName(String name);
}
