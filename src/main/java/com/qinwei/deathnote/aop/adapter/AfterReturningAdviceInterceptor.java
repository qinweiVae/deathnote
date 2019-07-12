package com.qinwei.deathnote.aop.adapter;

import com.qinwei.deathnote.aop.aspectj.AfterAdvice;
import com.qinwei.deathnote.aop.aspectj.AfterReturningAdvice;
import com.qinwei.deathnote.aop.intercept.MethodInterceptor;
import com.qinwei.deathnote.aop.intercept.MethodInvocation;

/**
 * @author qinwei
 * @date 2019-07-12
 */
public class AfterReturningAdviceInterceptor implements MethodInterceptor, AfterAdvice {

    private final AfterReturningAdvice advice;

    public AfterReturningAdviceInterceptor(AfterReturningAdvice advice) {
        this.advice = advice;
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        Object retVal = mi.proceed();
        this.advice.afterReturning(retVal, mi.getMethod(), mi.getArguments(), mi.getThis());
        return retVal;
    }
}
