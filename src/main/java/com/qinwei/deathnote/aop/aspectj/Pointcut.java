package com.qinwei.deathnote.aop.aspectj;

import com.qinwei.deathnote.aop.support.ClassFilter;
import com.qinwei.deathnote.aop.support.MethodMatcher;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public interface Pointcut {

    ClassFilter getClassFilter();

    MethodMatcher getMethodMatcher();

    Pointcut TRUE = new Pointcut() {
        @Override
        public ClassFilter getClassFilter() {
            return ClassFilter.TRUE;
        }

        @Override
        public MethodMatcher getMethodMatcher() {
            return MethodMatcher.TRUE;
        }
    };
}
