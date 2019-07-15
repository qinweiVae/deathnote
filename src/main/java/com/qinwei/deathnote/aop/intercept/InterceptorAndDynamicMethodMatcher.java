package com.qinwei.deathnote.aop.intercept;

import com.qinwei.deathnote.aop.support.MethodMatcher;

/**
 * @author qinwei
 * @date 2019-07-12
 */
public class InterceptorAndDynamicMethodMatcher {

    final MethodInterceptor interceptor;

    final MethodMatcher methodMatcher;

    public InterceptorAndDynamicMethodMatcher(MethodInterceptor interceptor, MethodMatcher methodMatcher) {
        this.interceptor = interceptor;
        this.methodMatcher = methodMatcher;
    }
}