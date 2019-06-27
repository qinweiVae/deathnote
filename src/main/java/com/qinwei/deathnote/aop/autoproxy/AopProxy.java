package com.qinwei.deathnote.aop.autoproxy;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public interface AopProxy {

    /**
     * 创建代理对象
     */
    Object getProxy();

    Object getProxy(ClassLoader classLoader);
}
