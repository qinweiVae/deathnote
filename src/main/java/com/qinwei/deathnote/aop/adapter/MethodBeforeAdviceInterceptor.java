package com.qinwei.deathnote.aop.adapter;

import com.qinwei.deathnote.aop.aspectj.BeforeAdvice;
import com.qinwei.deathnote.aop.aspectj.MethodBeforeAdvice;
import com.qinwei.deathnote.aop.intercept.MethodInterceptor;
import com.qinwei.deathnote.aop.intercept.MethodInvocation;

/**
 * @author qinwei
 * @date 2019-07-12
 */
public class MethodBeforeAdviceInterceptor implements MethodInterceptor, BeforeAdvice {

    private final MethodBeforeAdvice advice;

    public MethodBeforeAdviceInterceptor(MethodBeforeAdvice advice) {
        this.advice = advice;
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        this.advice.before(mi.getMethod(), mi.getArguments(), mi.getThis());
        return mi.proceed();
    }
}
