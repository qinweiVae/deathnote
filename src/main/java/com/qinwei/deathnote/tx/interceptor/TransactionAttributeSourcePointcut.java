package com.qinwei.deathnote.tx.interceptor;

import com.qinwei.deathnote.aop.aspectj.Pointcut;
import com.qinwei.deathnote.aop.support.ClassFilter;
import com.qinwei.deathnote.aop.support.MethodMatcher;
import com.qinwei.deathnote.tx.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;

/**
 * @author qinwei
 * @date 2019-08-02
 */
abstract class TransactionAttributeSourcePointcut implements Pointcut, MethodMatcher {

    private ClassFilter classFilter = ClassFilter.TRUE;

    @Override
    public ClassFilter getClassFilter() {
        return this.classFilter;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return this;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (PlatformTransactionManager.class.isAssignableFrom(targetClass)) {
            return false;
        }
        TransactionAttributeSource tas = getTransactionAttributeSource();
        // 拿到 方法或者 类 里面的 TransactionAttribute,如果存在则 进行 拦截
        return tas == null || tas.getTransactionAttribute(method, targetClass) != null;
    }

    @Override
    public boolean isRuntime() {
        return false;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass, Object... args) {
        throw new UnsupportedOperationException("Illegal MethodMatcher usage");
    }

    protected abstract TransactionAttributeSource getTransactionAttributeSource();
}
