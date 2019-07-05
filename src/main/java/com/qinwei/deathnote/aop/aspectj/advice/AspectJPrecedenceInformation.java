package com.qinwei.deathnote.aop.aspectj.advice;

/**
 * @author qinwei
 * @date 2019-07-05
 */
public interface AspectJPrecedenceInformation {

    String getAspectName();

    int getDeclarationOrder();

    boolean isBeforeAdvice();

    boolean isAfterAdvice();

}
