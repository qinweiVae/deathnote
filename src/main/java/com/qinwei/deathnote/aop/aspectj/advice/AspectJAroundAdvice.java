package com.qinwei.deathnote.aop.aspectj.advice;

import com.qinwei.deathnote.aop.AspectInstanceFactory;
import com.qinwei.deathnote.aop.aspectj.AspectJExpressionPointcut;
import com.qinwei.deathnote.aop.intercept.MethodInterceptor;
import com.qinwei.deathnote.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author qinwei
 * @date 2019-07-02
 */
public class AspectJAroundAdvice extends AbstractAspectJAdvice implements MethodInterceptor {


    public AspectJAroundAdvice(Method aspectJAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aspectInstanceFactory) {
        super(aspectJAdviceMethod, pointcut, aspectInstanceFactory);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return null;
    }
}
