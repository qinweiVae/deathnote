package com.qinwei.deathnote.aop.annotation;

import com.qinwei.deathnote.aop.AspectInstanceFactory;
import com.qinwei.deathnote.aop.aspectj.Advice;
import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.aspectj.AspectJExpressionPointcut;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author qinwei
 * @date 2019-07-01
 */
public interface AspectJAdvisorFactory {

    /**
     * 判断class 上是否有 @Aspect 注解
     */
    boolean isAspect(Class<?> clazz);

    /**
     * 如果 class 的父类有 @Aspect 注解 并且不是抽象类 则抛异常
     * <p>
     * 如果 class 上 的  @Aspect 注解 值不为空 ，则抛异常
     */
    void validate(Class<?> aspectClass);

    /**
     * 获取通知器
     */
    List<Advisor> getAdvisors(AspectInstanceFactory aspectInstanceFactory);

    /**
     * 获取通知器
     */
    Advisor getAdvisor(Method adviceMethod, AspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName);

    Advice getAdvice(Method adviceMethod, AspectJExpressionPointcut expressionPointcut,
                     AspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName);
}
