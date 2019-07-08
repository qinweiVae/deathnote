package com.qinwei.deathnote.aop.aspectj.advice;

/**
 * @author qinwei
 * @date 2019-07-05
 */
public interface AspectJPrecedenceInformation {

    /**
     * 就是配置了@Aspect 注解 的 bean name
     */
    String getAspectName();

    /**
     * 用于 排序
     */
    int getDeclarationOrder();

    boolean isBeforeAdvice();

    boolean isAfterAdvice();

}
