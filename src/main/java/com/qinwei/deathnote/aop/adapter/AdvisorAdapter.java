package com.qinwei.deathnote.aop.adapter;

import com.qinwei.deathnote.aop.aspectj.Advice;
import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.intercept.MethodInterceptor;

/**
 * @author qinwei
 * @date 2019-07-12
 */
public interface AdvisorAdapter {

    boolean supportsAdvice(Advice advice);

    MethodInterceptor getInterceptor(Advisor advisor);
}
