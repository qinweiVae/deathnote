package com.qinwei.deathnote.aop.aspectj.advice;

import com.qinwei.deathnote.aop.AspectInstanceFactory;
import com.qinwei.deathnote.aop.aspectj.Advice;
import com.qinwei.deathnote.aop.aspectj.AspectJExpressionPointcut;

import java.lang.reflect.Method;

/**
 * @author qinwei
 * @date 2019-07-02
 */
public class AbstractAspectJAdvice implements Advice {

    protected transient Method aspectJAdviceMethod;

    private final AspectJExpressionPointcut pointcut;

    private final AspectInstanceFactory aspectInstanceFactory;

    private String aspectName = "";

    private int declarationOrder;

    public AbstractAspectJAdvice(Method aspectJAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aspectInstanceFactory) {
        this.aspectJAdviceMethod = aspectJAdviceMethod;
        this.pointcut = pointcut;
        this.aspectInstanceFactory = aspectInstanceFactory;
    }

    public void setAspectName(String name) {
        this.aspectName = name;
    }

    public String getAspectName() {
        return this.aspectName;
    }

    public void setDeclarationOrder(int order) {
        this.declarationOrder = order;
    }

    public int getDeclarationOrder() {
        return this.declarationOrder;
    }
}
