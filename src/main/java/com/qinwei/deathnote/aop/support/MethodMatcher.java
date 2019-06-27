package com.qinwei.deathnote.aop.support;

import java.lang.reflect.Method;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public interface MethodMatcher {

    boolean matches(Method method, Class<?> targetClass);

    boolean isRuntime();

    boolean matches(Method method, Class<?> targetClass, Object... args);

    MethodMatcher TRUE = new MethodMatcher() {
        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return true;
        }

        @Override
        public boolean isRuntime() {
            return false;
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass, Object... args) {
            throw new UnsupportedOperationException();
        }
    };
}
