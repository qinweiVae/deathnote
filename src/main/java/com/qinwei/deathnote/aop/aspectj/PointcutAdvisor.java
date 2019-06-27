package com.qinwei.deathnote.aop.aspectj;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public interface PointcutAdvisor extends Advisor {

    Pointcut getPointcut();
}
