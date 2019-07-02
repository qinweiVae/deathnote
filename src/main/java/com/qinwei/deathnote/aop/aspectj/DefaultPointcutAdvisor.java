package com.qinwei.deathnote.aop.aspectj;

/**
 * @author qinwei
 * @date 2019-06-28
 */
public class DefaultPointcutAdvisor implements PointcutAdvisor {

    private Pointcut pointcut = Pointcut.TRUE;

    private Advice advice = EMPTY_ADVICE;

    public DefaultPointcutAdvisor() {
    }

    public DefaultPointcutAdvisor(Advice advice) {
        this(Pointcut.TRUE, advice);
    }

    public DefaultPointcutAdvisor(Pointcut pointcut, Advice advice) {
        this.pointcut = pointcut;
        setAdvice(advice);
    }

    public void setAdvice(Advice advice) {
        this.advice = advice;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    public void setPointcut(Pointcut pointcut) {
        this.pointcut = (pointcut != null ? pointcut : Pointcut.TRUE);
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

}
