package com.qinwei.deathnote.tx.interceptor;

import com.qinwei.deathnote.aop.intercept.MethodInterceptor;
import com.qinwei.deathnote.aop.intercept.MethodInvocation;
import com.qinwei.deathnote.aop.support.AopUtils;
import com.qinwei.deathnote.tx.support.TransactionAspectSupport;

/**
 * @author qinwei
 * @date 2019-07-31
 */
public class TransactionInterceptor extends TransactionAspectSupport implements MethodInterceptor {

    public TransactionInterceptor() {
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);

        return invokeWithinTransaction(invocation.getMethod(), targetClass, invocation::proceed);
    }
}
