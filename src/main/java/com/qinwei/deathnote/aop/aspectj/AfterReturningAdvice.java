package com.qinwei.deathnote.aop.aspectj;

import java.lang.reflect.Method;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public interface AfterReturningAdvice extends AfterAdvice {

    void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable;
}
