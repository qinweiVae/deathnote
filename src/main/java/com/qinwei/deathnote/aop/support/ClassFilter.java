package com.qinwei.deathnote.aop.support;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public interface ClassFilter {

    /**
     * 对类进行过滤匹配,ClassFilter指的@Aspect注解中使用的切点表达式
     */
    boolean matches(Class<?> clazz);

    ClassFilter TRUE = clazz -> true;
}
