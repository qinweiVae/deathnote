package com.qinwei.deathnote.aop.adapter;

import com.qinwei.deathnote.aop.aspectj.Advice;
import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.aspectj.ThrowsAdvice;
import com.qinwei.deathnote.aop.intercept.MethodInterceptor;

/**
 * @author qinwei
 * @date 2019-07-12
 */
public class ThrowsAdviceAdapter implements AdvisorAdapter {

    @Override
    public boolean supportsAdvice(Advice advice) {
        return advice instanceof ThrowsAdvice;
    }

    @Override
    public MethodInterceptor getInterceptor(Advisor advisor) {
        return new ThrowsAdviceInterceptor(advisor.getAdvice());
    }
}
