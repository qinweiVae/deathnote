package com.qinwei.deathnote.aop.intercept;

import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.aspectj.DefaultPointcutAdvisor;
import com.qinwei.deathnote.context.annotation.Order;

import java.io.Serializable;

/**
 * @author qinwei
 * @date 2019-07-05
 */
@Order(value = Integer.MIN_VALUE + 1)
public class ExposeInvocationInterceptor implements MethodInterceptor, Serializable {

    public static final ExposeInvocationInterceptor INSTANCE = new ExposeInvocationInterceptor();

    private static final ThreadLocal<MethodInvocation> INVOCATION = new ThreadLocal<>();

    public static final Advisor ADVISOR = new DefaultPointcutAdvisor(INSTANCE);

    private ExposeInvocationInterceptor() {
    }

    public static MethodInvocation currentInvocation() throws IllegalStateException {
        MethodInvocation methodInvocation = INVOCATION.get();
        if (methodInvocation == null) {
            throw new IllegalStateException(
                    "No MethodInvocation found: Check that an AOP INVOCATION is in progress, and that the " +
                            "ExposeInvocationInterceptor is upfront in the interceptor chain. Specifically, note that " +
                            "advices with order HIGHEST_PRECEDENCE will execute before ExposeInvocationInterceptor!");
        }
        return methodInvocation;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        MethodInvocation oldInvocation = INVOCATION.get();
        INVOCATION.set(invocation);
        try {
            return invocation.proceed();
        } finally {
            INVOCATION.set(oldInvocation);
        }
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
