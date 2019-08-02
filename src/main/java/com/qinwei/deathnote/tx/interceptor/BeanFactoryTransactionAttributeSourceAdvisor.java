package com.qinwei.deathnote.tx.interceptor;

import com.qinwei.deathnote.aop.aspectj.Advice;
import com.qinwei.deathnote.aop.aspectj.Pointcut;
import com.qinwei.deathnote.aop.aspectj.PointcutAdvisor;

/**
 * @author qinwei
 * @date 2019-08-02
 */
public class BeanFactoryTransactionAttributeSourceAdvisor implements PointcutAdvisor {

    private TransactionAttributeSource transactionAttributeSource;

    private transient volatile Advice advice;

    private transient volatile Object adviceMonitor = new Object();

    private final TransactionAttributeSourcePointcut pointcut = new TransactionAttributeSourcePointcut() {
        @Override
        protected TransactionAttributeSource getTransactionAttributeSource() {
            return transactionAttributeSource;
        }
    };

    public void setTransactionAttributeSource(TransactionAttributeSource transactionAttributeSource) {
        this.transactionAttributeSource = transactionAttributeSource;
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    public void setAdvice(Advice advice) {
        synchronized (this.adviceMonitor) {
            this.advice = advice;
        }
    }
}
