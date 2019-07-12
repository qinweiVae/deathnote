package com.qinwei.deathnote.aop.adapter;

import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.intercept.MethodInterceptor;

/**
 * @author qinwei
 * @date 2019-07-12
 */
public interface AdvisorAdapterRegistry {

    Advisor wrap(Object advice);

    MethodInterceptor[] getInterceptors(Advisor advisor);

    void registerAdvisorAdapter(AdvisorAdapter adapter);
}
