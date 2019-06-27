package com.qinwei.deathnote.aop.support;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public interface ClassFilter {

    boolean matches(Class<?> clazz);

    ClassFilter TRUE = clazz -> true;
}
