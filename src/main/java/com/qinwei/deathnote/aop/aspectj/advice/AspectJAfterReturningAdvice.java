package com.qinwei.deathnote.aop.aspectj.advice;

import com.qinwei.deathnote.aop.AspectInstanceFactory;
import com.qinwei.deathnote.aop.aspectj.AfterAdvice;
import com.qinwei.deathnote.aop.aspectj.AfterReturningAdvice;
import com.qinwei.deathnote.aop.aspectj.AspectJExpressionPointcut;

import java.lang.reflect.Method;

/**
 * @author qinwei
 * @date 2019-07-02
 */
public class AspectJAfterReturningAdvice extends AbstractAspectJAdvice implements AfterReturningAdvice, AfterAdvice {

    public AspectJAfterReturningAdvice(Method aspectJAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aspectInstanceFactory) {
        super(aspectJAdviceMethod, pointcut, aspectInstanceFactory);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {

    }
}
