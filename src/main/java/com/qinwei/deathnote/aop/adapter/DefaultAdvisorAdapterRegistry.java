package com.qinwei.deathnote.aop.adapter;

import com.qinwei.deathnote.aop.aspectj.Advice;
import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.aspectj.DefaultPointcutAdvisor;
import com.qinwei.deathnote.aop.intercept.MethodInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qinwei
 * @date 2019-07-12
 */
public class DefaultAdvisorAdapterRegistry implements AdvisorAdapterRegistry {

    private final List<AdvisorAdapter> adapters = new ArrayList<>(3);

    public DefaultAdvisorAdapterRegistry() {
        registerAdvisorAdapter(new MethodBeforeAdviceAdapter());
        registerAdvisorAdapter(new AfterReturningAdviceAdapter());
        registerAdvisorAdapter(new ThrowsAdviceAdapter());
    }

    @Override
    public Advisor wrap(Object adviceObject) {
        if (adviceObject instanceof Advisor) {
            return (Advisor) adviceObject;
        }
        if (!(adviceObject instanceof Advice)) {
            throw new IllegalStateException("Advice object [" + adviceObject + "] is neither a supported subinterface of " +
                    "[org.aopalliance.aop.Advice] nor an [org.springframework.aop.Advisor]");
        }
        Advice advice = (Advice) adviceObject;
        if (advice instanceof MethodInterceptor) {
            return new DefaultPointcutAdvisor(advice);
        }
        for (AdvisorAdapter adapter : this.adapters) {
            if (adapter.supportsAdvice(advice)) {
                return new DefaultPointcutAdvisor(advice);
            }
        }
        throw new IllegalStateException("Advice object [" + advice + "] is neither a supported subinterface of " +
                "[org.aopalliance.aop.Advice] nor an [org.springframework.aop.Advisor]");
    }

    @Override
    public MethodInterceptor[] getInterceptors(Advisor advisor) {
        List<MethodInterceptor> interceptors = new ArrayList<>(3);
        Advice advice = advisor.getAdvice();
        if (advice instanceof MethodInterceptor) {
            interceptors.add((MethodInterceptor) advice);
        }
        for (AdvisorAdapter adapter : this.adapters) {
            if (adapter.supportsAdvice(advice)) {
                interceptors.add(adapter.getInterceptor(advisor));
            }
        }
        if (interceptors.isEmpty()) {
            throw new IllegalStateException("Advice object [" + advisor.getAdvice() + "] is neither a supported subinterface of " +
                    "[org.aopalliance.aop.Advice] nor an [org.springframework.aop.Advisor]");
        }
        return interceptors.toArray(new MethodInterceptor[0]);
    }

    @Override
    public void registerAdvisorAdapter(AdvisorAdapter adapter) {
        this.adapters.add(adapter);
    }
}
