package com.qinwei.deathnote.tx.support;

import com.qinwei.deathnote.beans.factory.BeanFactory;
import com.qinwei.deathnote.context.aware.BeanFactoryAware;
import com.qinwei.deathnote.tx.interceptor.TransactionAttribute;
import com.qinwei.deathnote.tx.interceptor.TransactionAttributeSource;
import com.qinwei.deathnote.tx.transaction.PlatformTransactionManager;
import com.qinwei.deathnote.tx.transaction.TransactionStatus;
import com.qinwei.deathnote.utils.StringUtils;

import java.lang.reflect.Method;

/**
 * @author qinwei
 * @date 2019-07-31
 */
public abstract class TransactionAspectSupport implements BeanFactoryAware {

    private static final ThreadLocal<TransactionInfo> transactionInfoHolder = new ThreadLocal<>();

    private TransactionAttributeSource transactionAttributeSource;

    private BeanFactory beanFactory;

    public void setTransactionAttributeSource(TransactionAttributeSource transactionAttributeSource) {
        this.transactionAttributeSource = transactionAttributeSource;
    }

    public TransactionAttributeSource getTransactionAttributeSource() {
        return this.transactionAttributeSource;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    protected Object invokeWithinTransaction(Method method, Class<?> targetClass, final InvocationCallback invocation) throws Throwable {
        TransactionAttributeSource tas = getTransactionAttributeSource();
        //拿到 @Transactional 的属性值
        final TransactionAttribute txAttr = tas != null ? tas.getTransactionAttribute(method, targetClass) : null;
        //找到合适的 PlatformTransactionManager
        final PlatformTransactionManager tm = determineTransactionManager(txAttr);
        // 拿到方法标识，即 类名.方法名
        /*final String joinpointIdentification = txAttr instanceof DefaultTransactionAttribute ?
                ((DefaultTransactionAttribute) txAttr).getDescriptor() :
                ClassUtils.getQualifiedMethodName(method, targetClass);*/

        //创建 TransactionInfo
        TransactionInfo txInfo = createTransactionIfNecessary(tm, txAttr);

        Object retVal;
        try {
            //执行 aop 拦截器链
            retVal = invocation.proceedWithInvocation();
        } catch (Throwable ex) {
            //如果 异常 需要回滚 则 回滚事物 ，否则 提交事物
            completeTransactionAfterThrowing(txInfo, ex);
            throw ex;
        } finally {
            //如果配置了 @Transactional, ThreadLocal恢复原来的 TransactionInfo
            if (txInfo != null) {
                txInfo.restoreThreadLocalStatus();
            }
        }
        //如果没有配置@Transactional ，或者 配置了事物 正常执行，提交事物
        if (txInfo != null && txInfo.getTransactionStatus() != null) {
            txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
        }
        return retVal;
    }

    protected void completeTransactionAfterThrowing(TransactionInfo txInfo, Throwable ex) {
        TransactionStatus status = txInfo.getTransactionStatus();
        //如果配置了 @Transactional
        if (status != null) {
            // 如果 异常 需要 回滚
            if (txInfo.transactionAttribute != null && txInfo.transactionAttribute.rollbackOn(ex)) {
                //事物回滚
                txInfo.getTransactionManager().rollback(status);
            } else {
                // 否则 提交事物
                txInfo.getTransactionManager().commit(status);
            }
        }
    }

    protected TransactionInfo createTransactionIfNecessary(PlatformTransactionManager tm, TransactionAttribute txAttr) {
        TransactionStatus status = null;
        //如果配置了 @Transactional ，并且配置了 PlatformTransactionManager 的 bean
        if (txAttr != null && tm != null) {
            status = tm.getTransaction(txAttr);
        }
        return prepareTransactionInfo(tm, txAttr, status);
    }

    private TransactionInfo prepareTransactionInfo(PlatformTransactionManager tm, TransactionAttribute txAttr, TransactionStatus status) {
        TransactionInfo txInfo = new TransactionInfo(tm, txAttr);
        //如果配置了 @Transactional
        if (txAttr != null) {
            txInfo.newTransactionStatus(status);
        }
        //绑定到 ThreadLocal
        txInfo.bindToThread();
        return txInfo;
    }

    protected PlatformTransactionManager determineTransactionManager(TransactionAttribute txAttr) {
        // 如果没有 @Transactional
        if (txAttr == null) {
            return null;
        }
        // 否则拿到 @Transactional 中的 transactionManager 属性
        String qualifier = txAttr.getQualifier();
        //如果配置了 transactionManager，根据 beanName 去找
        if (StringUtils.isNotEmpty(qualifier)) {
            return this.beanFactory.getBean(qualifier, PlatformTransactionManager.class);
        }
        // 如果没有配置 transactionManager，根据类型去找
        else {
            return this.beanFactory.getBean(PlatformTransactionManager.class);
        }
    }

    @FunctionalInterface
    protected interface InvocationCallback {

        Object proceedWithInvocation() throws Throwable;
    }

    protected final class TransactionInfo {

        private final PlatformTransactionManager transactionManager;

        private final TransactionAttribute transactionAttribute;

        private TransactionStatus transactionStatus;

        private TransactionInfo oldTransactionInfo;

        public TransactionInfo(PlatformTransactionManager transactionManager, TransactionAttribute transactionAttribute) {
            this.transactionManager = transactionManager;
            this.transactionAttribute = transactionAttribute;
        }

        public PlatformTransactionManager getTransactionManager() {
            return this.transactionManager;
        }

        public TransactionAttribute getTransactionAttribute() {
            return this.transactionAttribute;
        }

        public void newTransactionStatus(TransactionStatus status) {
            this.transactionStatus = status;
        }

        public TransactionStatus getTransactionStatus() {
            return this.transactionStatus;
        }

        public boolean hasTransaction() {
            return this.transactionStatus != null;
        }

        private void bindToThread() {
            this.oldTransactionInfo = transactionInfoHolder.get();
            transactionInfoHolder.set(this);
        }

        private void restoreThreadLocalStatus() {
            transactionInfoHolder.set(this.oldTransactionInfo);
        }

    }

}
