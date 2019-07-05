package com.qinwei.deathnote.aop.aspectj.advice;

import com.qinwei.deathnote.aop.AspectInstanceFactory;
import com.qinwei.deathnote.aop.aspectj.AfterAdvice;
import com.qinwei.deathnote.aop.aspectj.AfterReturningAdvice;
import com.qinwei.deathnote.aop.aspectj.AspectJExpressionPointcut;
import com.qinwei.deathnote.utils.ClassUtils;

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
        if (shouldInvokeOnReturnValue(method, returnValue)) {
            invokeAdviceMethod(getJoinPointMatch(), returnValue, null);
        }
    }

    private boolean shouldInvokeOnReturnValue(Method method, Object returnValue) {
        Class<?> returningType = getReturningType();
        if (returnValue != null) {
            return ClassUtils.isAssignable(returningType, returnValue.getClass());
        } else if (Object.class == returningType && void.class == method.getReturnType()) {
            return true;
        } else {
            return ClassUtils.isAssignable(returningType, method.getReturnType());
        }
    }

    @Override
    public void setReturningName(String name) {
        setReturningNameNoCheck(name);
    }

    @Override
    public boolean isBeforeAdvice() {
        return false;
    }

    @Override
    public boolean isAfterAdvice() {
        return true;
    }
}
