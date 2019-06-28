package com.qinwei.deathnote.aop.aspectj;

/**
 * @author qinwei
 * @date 2019-06-28
 */
public abstract class AbstractPointcutAdvisor implements PointcutAdvisor {

    private Advice advice = EMPTY_ADVICE;

    public void setAdvice(Advice advice) {
        this.advice = advice;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }
}
