package com.qinwei.deathnote.aop.intercept;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public interface ProxyMethodInvocation extends MethodInvocation {

    Object getProxy();
}
