package com.qinwei.deathnote.tx.transaction;

import com.qinwei.deathnote.tx.support.DefaultTransactionStatus;
import com.qinwei.deathnote.tx.support.TransactionSynchronization;
import com.qinwei.deathnote.tx.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * @author qinwei
 * @date 2019-08-02
 */
public abstract class AbstractPlatformTransactionManager implements PlatformTransactionManager {

    public static final int SYNCHRONIZATION_ALWAYS = 0;

    public static final int SYNCHRONIZATION_ON_ACTUAL_TRANSACTION = 1;

    public static final int SYNCHRONIZATION_NEVER = 2;

    private int transactionSynchronization = SYNCHRONIZATION_ALWAYS;

    private int defaultTimeout = TransactionDefinition.TIMEOUT_DEFAULT;

    private boolean nestedTransactionAllowed = false;

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) {
        // 获取 事务 对象
        Object transaction = doGetTransaction();
        // 如果definition为空，创建默认的
        if (definition == null) {
            definition = new DefaultTransactionDefinition();
        }
        // 如果存在事务了，检查 传播行为，根据不同的传播行为做相应的处理
        if (isExistingTransaction(transaction)) {
            return handleExistingTransaction(definition, transaction);
        }
        //检查设置的超时时间，不能小于 -1
        if (definition.getTimeout() < TransactionDefinition.TIMEOUT_DEFAULT) {
            throw new IllegalArgumentException("Invalid transaction timeout : " + definition.getTimeout());
        }
        //走到这一步说明不存在事务，如果传播行为是 PROPAGATION_MANDATORY 说明必须在事务里面，那么直接抛异常
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY) {
            throw new IllegalStateException("No existing transaction found for transaction marked with propagation 'mandatory'");
        }
        //如果传播行为是 PROPAGATION_REQUIRED 、PROPAGATION_REQUIRES_NEW、PROPAGATION_NESTED
        else if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||
                definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW ||
                definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {

        }
    }

    /**
     * 如果已经存在事务了
     */
    private TransactionStatus handleExistingTransaction(TransactionDefinition definition, Object transaction) {
        //如果传播行为是 PROPAGATION_NEVER ，直接抛异常
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NEVER) {
            throw new IllegalStateException("Existing transaction found for transaction marked with propagation 'never'");
        }
        //如果传播行为是 PROPAGATION_NOT_SUPPORTED, 挂起当前事务
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED) {
            Object suspendedResources = suspend(transaction);
            boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
            //
            return prepareTransactionStatus(definition, null, false, newSynchronization, suspendedResources);
        }
        //如果传播行为是 PROPAGATION_REQUIRES_NEW
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW) {

        }
        return null;
    }

    protected final DefaultTransactionStatus prepareTransactionStatus(
            TransactionDefinition definition, Object transaction, boolean newTransaction,
            boolean newSynchronization, Object suspendedResources) {

        DefaultTransactionStatus status = newTransactionStatus(definition, transaction, newTransaction, newSynchronization, suspendedResources);
        prepareSynchronization(status, definition);
        return status;
    }

    private DefaultTransactionStatus newTransactionStatus(TransactionDefinition definition, Object transaction,
                                                          boolean newTransaction, boolean newSynchronization, Object suspendedResources) {

        return null;
    }

    /**
     * 挂起当前事务
     */
    protected final SuspendedResourcesHolder suspend(Object transaction) {
        //如果当前线程的 事务同步开启了
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // 挂起当前线程所有的事务同步并关闭事务同步
            List<TransactionSynchronization> suspendedSynchronizations = doSuspendSynchronization();
            try {
                Object suspendedResources = null;
                // 如果存在事务, 挂起
                if (transaction != null) {
                    suspendedResources = doSuspend(transaction);
                }
                // 将当前线程中的事务 name 设置为 null
                String name = TransactionSynchronizationManager.getCurrentTransactionName();
                TransactionSynchronizationManager.setCurrentTransactionName(null);
                // 将当前线程中的事务 readonly 设置为 false
                boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
                TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
                // 将当前线程中的事务 隔离级别 设置为 null
                Integer isolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
                TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(null);
                //  将当前线程中的事务同步 设置为false
                boolean wasActive = TransactionSynchronizationManager.isActualTransactionActive();
                TransactionSynchronizationManager.setActualTransactionActive(false);
                // 创建 SuspendedResourcesHolder
                return new SuspendedResourcesHolder(suspendedResources, suspendedSynchronizations, name, readOnly, isolationLevel, wasActive);

            } catch (RuntimeException | Error ex) {
                doResumeSynchronization(suspendedSynchronizations);
                throw ex;
            }
        }
        // 如果存在事务 ，但是 事务同步 没有开启
        else if (transaction != null) {
            // 挂起事务
            Object suspendedResources = doSuspend(transaction);
            // 创建 SuspendedResourcesHolder
            return new SuspendedResourcesHolder(suspendedResources);
        } else {
            return null;
        }
    }

    /**
     * 挂起当前线程所有的事务同步并关闭事务同步
     */
    private List<TransactionSynchronization> doSuspendSynchronization() {
        //从ThreadLocal中拿到当前线程的所有 事务同步
        List<TransactionSynchronization> suspendedSynchronizations = TransactionSynchronizationManager.getSynchronizations();
        //挂起事务同步
        for (TransactionSynchronization synchronization : suspendedSynchronizations) {
            synchronization.suspend();
        }
        //把 ThreadLocal 里面所有的 事务同步 移除
        TransactionSynchronizationManager.clearSynchronization();
        return suspendedSynchronizations;
    }

    /**
     * 恢复当前线程中的所有事务同步
     */
    private void doResumeSynchronization(List<TransactionSynchronization> suspendedSynchronizations) {
        TransactionSynchronizationManager.initSynchronization();
        for (TransactionSynchronization synchronization : suspendedSynchronizations) {
            // 恢复 事务同步
            synchronization.resume();
            // 放到 ThreadLocal 中
            TransactionSynchronizationManager.registerSynchronization(synchronization);
        }
    }

    @Override
    public void commit(TransactionStatus status) {

    }

    @Override
    public void rollback(TransactionStatus status) {

    }

    protected boolean isExistingTransaction(Object transaction) {
        return false;
    }

    protected Object doSuspend(Object transaction) {
        throw new UnsupportedOperationException("Transaction manager [" + getClass().getName() + "] does not support transaction suspension");
    }

    public final int getDefaultTimeout() {
        return this.defaultTimeout;
    }

    public final void setNestedTransactionAllowed(boolean nestedTransactionAllowed) {
        this.nestedTransactionAllowed = nestedTransactionAllowed;
    }

    public final boolean isNestedTransactionAllowed() {
        return this.nestedTransactionAllowed;
    }

    public final void setTransactionSynchronization(int transactionSynchronization) {
        this.transactionSynchronization = transactionSynchronization;
    }

    public final int getTransactionSynchronization() {
        return this.transactionSynchronization;
    }

    protected abstract Object doGetTransaction();


    protected static final class SuspendedResourcesHolder {

        private final Object suspendedResources;

        private List<TransactionSynchronization> suspendedSynchronizations;

        private String name;

        private boolean readOnly;

        private Integer isolationLevel;

        private boolean wasActive;

        private SuspendedResourcesHolder(Object suspendedResources) {
            this.suspendedResources = suspendedResources;
        }

        private SuspendedResourcesHolder(
                Object suspendedResources, List<TransactionSynchronization> suspendedSynchronizations,
                String name, boolean readOnly, Integer isolationLevel, boolean wasActive) {

            this.suspendedResources = suspendedResources;
            this.suspendedSynchronizations = suspendedSynchronizations;
            this.name = name;
            this.readOnly = readOnly;
            this.isolationLevel = isolationLevel;
            this.wasActive = wasActive;
        }
    }
}
