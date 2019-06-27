package com.qinwei.deathnote.aop.intercept;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public interface MethodInterceptor extends Interceptor {

    Object invoke(MethodInvocation invocation) throws Throwable;
}