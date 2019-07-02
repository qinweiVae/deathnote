package com.qinwei.deathnote.aop.annotation;

import com.qinwei.deathnote.aop.AspectInstanceFactory;
import com.qinwei.deathnote.aop.aspectj.Advice;
import com.qinwei.deathnote.aop.aspectj.AspectJExpressionPointcut;
import com.qinwei.deathnote.aop.aspectj.Pointcut;
import com.qinwei.deathnote.aop.aspectj.PointcutAdvisor;

import java.lang.reflect.Method;

/**
 * @author qinwei
 * @date 2019-07-02
 */
public class PointcutAdvisorImpl implements PointcutAdvisor {

    private final AspectJExpressionPointcut pointcut;

    private transient Method adviceMethod;

    private final AspectJAdvisorFactory advisorFactory;

    private final int declarationOrder;

    private final String aspectName;

    private AspectInstanceFactory aspectInstanceFactory;

    private Advice instantiatedAdvice;

    public PointcutAdvisorImpl(AspectJExpressionPointcut expressionPointcut, Method adviceMethod,
                               AspectJAdvisorFactory advisorFactory, AspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName) {
        this.pointcut = expressionPointcut;
        this.adviceMethod = adviceMethod;
        this.advisorFactory = advisorFactory;
        this.declarationOrder = declarationOrder;
        this.aspectName = aspectName;
        this.aspectInstanceFactory = aspectInstanceFactory;
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        if (this.instantiatedAdvice == null) {
            this.instantiatedAdvice = instantiateAdvice(this.pointcut);
        }
        return this.instantiatedAdvice;
    }

    private Advice instantiateAdvice(AspectJExpressionPointcut pointcut) {
        Advice advice = this.advisorFactory.getAdvice(this.adviceMethod, this.pointcut, this.aspectInstanceFactory, declarationOrder, aspectName);
        return (advice != null ? advice : EMPTY_ADVICE);
    }
}
