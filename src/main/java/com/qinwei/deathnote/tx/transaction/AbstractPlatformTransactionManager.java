package com.qinwei.deathnote.tx.transaction;

import com.qinwei.deathnote.tx.support.DefaultTransactionStatus;
import com.qinwei.deathnote.tx.support.TransactionSynchronization;
import com.qinwei.deathnote.tx.support.TransactionSynchronizationManager;
import com.qinwei.deathnote.tx.support.TransactionSynchronizationUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author qinwei
 * @date 2019-08-02
 */
@Slf4j
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
            // 挂起事务
            SuspendedResourcesHolder suspendedResources = suspend(null);
            try {
                // newSynchronization 是 true
                boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
                DefaultTransactionStatus status = newTransactionStatus(definition, transaction, true, newSynchronization, suspendedResources);
                // 开始一个新的事务
                doBegin(transaction, definition);
                prepareSynchronization(status, definition);
                return status;
            } catch (RuntimeException | Error ex) {
                //恢复 事务 和 事务同步
                resume(null, suspendedResources);
                throw ex;
            }
        } else {
            //创建一个空的事务 ，并不是真正的事务
            boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
            // newSynchronization 是 true
            return prepareTransactionStatus(definition, null, true, newSynchronization, null);
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
            //挂起当前事务
            Object suspendedResources = suspend(transaction);
            //如果 transactionSynchronization 是 SYNCHRONIZATION_ALWAYS , 此处 newSynchronization 是 true
            boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
            //创建默认的 TransactionStatus , 初始化 transaction synchronization
            return prepareTransactionStatus(definition, null, false, newSynchronization, suspendedResources);
        }
        //如果传播行为是 PROPAGATION_REQUIRES_NEW, 挂起当前事务
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW) {
            //挂起当前事务
            SuspendedResourcesHolder suspendedResources = suspend(transaction);
            try {
                //如果 transactionSynchronization 不是 SYNCHRONIZATION_NEVER , 此处 newSynchronization 是 true
                boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
                //创建默认的 TransactionStatus , 初始化 transaction synchronization
                DefaultTransactionStatus status = newTransactionStatus(definition, transaction, true, newSynchronization, suspendedResources);
                // 开始一个新的事务
                doBegin(transaction, definition);
                prepareSynchronization(status, definition);
                return status;
            } catch (RuntimeException | Error beginEx) {
                // 如果异常了 恢复 事务 和 事务同步
                resumeAfterBeginException(transaction, suspendedResources, beginEx);
                throw beginEx;
            }
        }
        //如果传播行为是 PROPAGATION_NESTED
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
            //判断是否允许嵌套事务， nestedTransactionAllowed 默认 false
            if (!isNestedTransactionAllowed()) {
                throw new UnsupportedOperationException("Transaction manager does not allow nested transactions by default - " +
                        "specify 'nestedTransactionAllowed' property with value 'true'");
            }
            DefaultTransactionStatus status = prepareTransactionStatus(definition, transaction, false, false, null);
            status.createAndHoldSavepoint();
            return status;
        }
        // 如果传播行为是 PROPAGATION_SUPPORTS or PROPAGATION_REQUIRED.
        boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
        return prepareTransactionStatus(definition, transaction, false, newSynchronization, null);
    }

    private void resumeAfterBeginException(Object transaction, SuspendedResourcesHolder suspendedResources, Throwable beginEx) {
        try {
            resume(transaction, suspendedResources);
        } catch (RuntimeException | Error resumeEx) {
            log.error("Inner transaction begin exception overridden by outer transaction resume exception", beginEx);
            throw resumeEx;
        }
    }

    /**
     * 恢复 事务 和 事务同步
     */
    protected final void resume(Object transaction, SuspendedResourcesHolder resourcesHolder) {
        if (resourcesHolder != null) {
            Object suspendedResources = resourcesHolder.suspendedResources;
            if (suspendedResources != null) {
                // 恢复 事务
                doResume(transaction, suspendedResources);
            }
            List<TransactionSynchronization> suspendedSynchronizations = resourcesHolder.suspendedSynchronizations;
            if (suspendedSynchronizations != null) {
                TransactionSynchronizationManager.setActualTransactionActive(resourcesHolder.wasActive);
                TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(resourcesHolder.isolationLevel);
                TransactionSynchronizationManager.setCurrentTransactionReadOnly(resourcesHolder.readOnly);
                //恢复当前线程中的所有事务同步
                doResumeSynchronization(suspendedSynchronizations);
            }
        }
    }

    protected void doResume(Object transaction, Object suspendedResources) {
        throw new UnsupportedOperationException("Transaction manager [" + getClass().getName() + "] does not support transaction suspension");
    }

    protected final DefaultTransactionStatus prepareTransactionStatus(
            TransactionDefinition definition, Object transaction, boolean newTransaction,
            boolean newSynchronization, Object suspendedResources) {

        DefaultTransactionStatus status = newTransactionStatus(definition, transaction, newTransaction, newSynchronization, suspendedResources);
        prepareSynchronization(status, definition);
        return status;
    }

    /**
     * 按需初始化 TransactionSynchronization 的 ThreadLocal
     */
    private void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition) {
        if (status.isNewSynchronization()) {
            TransactionSynchronizationManager.setActualTransactionActive(status.hasTransaction());
            TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(
                    definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT ?
                            definition.getIsolationLevel() : null);
            TransactionSynchronizationManager.setCurrentTransactionReadOnly(definition.isReadOnly());
            TransactionSynchronizationManager.initSynchronization();
        }
    }

    /**
     * 根据传入的参数构造 TransactionStatus
     * 存在事务了 并且传播行为是 PROPAGATION_NESTED 的情况下, newSynchronization 为false ，其余情况都是 true
     * 存在事务了 并且传播行为是 PROPAGATION_NOT_SUPPORTED, PROPAGATION_NESTED, PROPAGATION_SUPPORTS, PROPAGATION_REQUIRED 的情况下,newTransaction 为 false,其余情况都是 true
     */
    private DefaultTransactionStatus newTransactionStatus(TransactionDefinition definition, Object transaction,
                                                          boolean newTransaction, boolean newSynchronization, Object suspendedResources) {
        //判断是否调用过 TransactionSynchronizationManager.initSynchronization()
        boolean actualNewSynchronization = newSynchronization && !TransactionSynchronizationManager.isSynchronizationActive();
        // 构造 TransactionStatus
        return new DefaultTransactionStatus(transaction, newTransaction, actualNewSynchronization, definition.isReadOnly(), suspendedResources);
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
                return new SuspendedResourcesHolder(suspendedResources, suspendedSynchronizations, readOnly, isolationLevel, wasActive);

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
        //如果此时事务已完成 ，抛异常
        if (status.isCompleted()) {
            throw new IllegalStateException("Transaction is already completed - do not call commit or rollback more than once per transaction");
        }
        DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
        if (defStatus.isLocalRollbackOnly()) {
            log.info("Transactional code has requested rollback");
            // 回滚
            processRollback(defStatus, false);
            return;
        }
        // 提交事务
        processCommit(defStatus);
    }

    @Override
    public void rollback(TransactionStatus status) {
        if (status.isCompleted()) {
            throw new IllegalStateException("Transaction is already completed - do not call commit or rollback more than once per transaction");
        }

        DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
        processRollback(defStatus, false);
    }

    private void processCommit(DefaultTransactionStatus status) {
        try {
            boolean beforeCompletionInvoked = false;

            try {
                triggerBeforeCommit(status);
                triggerBeforeCompletion(status);
                beforeCompletionInvoked = true;
                // 如果有 save point
                if (status.hasSavepoint()) {
                    log.debug("Releasing transaction savepoint");
                    status.releaseHeldSavepoint();
                }
                // 如果有 事务
                else if (status.hasTransaction()) {
                    log.debug("Initiating transaction commit");
                    // 提交事务
                    doCommit(status);
                }
            } catch (UnsupportedOperationException ex) {
                triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);
                throw ex;
            } catch (RuntimeException | Error ex) {
                if (!beforeCompletionInvoked) {
                    triggerBeforeCompletion(status);
                }
                doRollbackOnCommitException(status, ex);
                throw ex;
            }
            try {
                triggerAfterCommit(status);
            } finally {
                triggerAfterCompletion(status, TransactionSynchronization.STATUS_COMMITTED);
            }
        } finally {
            cleanupAfterCompletion(status);
        }
    }

    private void processRollback(DefaultTransactionStatus status, boolean unexpected) {
        try {
            boolean unexpectedRollback = unexpected;
            try {
                // 触发操作完之前
                triggerBeforeCompletion(status);
                //如果有 savepoint ,回滚 savepoint
                if (status.hasSavepoint()) {
                    log.debug("Rolling back transaction to savepoint");
                    status.rollbackToHeldSavepoint();
                }
                //如果开启 新事务，回滚事务
                else if (status.isNewTransaction()) {
                    log.debug("Initiating transaction rollback");
                    //回滚事务
                    doRollback(status);
                } else {
                    // 如果在事务中
                    if (status.hasTransaction()) {
                        //设置事务为 rollback-only , 默认是抛异常
                        doSetRollbackOnly(status);
                    } else {
                        log.debug("Should roll back transaction but cannot - no transaction available");
                    }
                    unexpectedRollback = false;
                }

            } catch (RuntimeException | Error ex) {
                triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
                throw ex;
            }
            triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);
            if (unexpectedRollback) {
                throw new UnsupportedOperationException("Transaction rolled back because it has been marked as rollback-only");
            }
        } finally {
            cleanupAfterCompletion(status);
        }
    }

    private void doRollbackOnCommitException(DefaultTransactionStatus status, Throwable ex) {
        try {
            if (status.isNewTransaction()) {
                log.debug("Initiating transaction rollback after commit exception", ex);
                doRollback(status);
            } else if (status.hasTransaction()) {
                log.debug("Marking existing transaction as rollback-only after commit exception", ex);
                doSetRollbackOnly(status);
            }
        } catch (RuntimeException | Error rbex) {
            log.error("Commit exception overridden by rollback exception", ex);
            triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
            throw rbex;
        }
        triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);
    }

    protected final void triggerBeforeCommit(DefaultTransactionStatus status) {
        if (status.isNewSynchronization()) {
            log.debug("Triggering beforeCommit synchronization");
            TransactionSynchronizationUtils.triggerBeforeCommit(status.isReadOnly());
        }
    }

    private void triggerAfterCommit(DefaultTransactionStatus status) {
        if (status.isNewSynchronization()) {
            log.debug("Triggering afterCommit synchronization");
            TransactionSynchronizationUtils.triggerAfterCommit();
        }
    }

    protected final void triggerBeforeCompletion(DefaultTransactionStatus status) {
        if (status.isNewSynchronization()) {
            log.debug("Triggering beforeCompletion synchronization");
            TransactionSynchronizationUtils.triggerBeforeCompletion();
        }
    }

    private void triggerAfterCompletion(DefaultTransactionStatus status, int completionStatus) {
        if (status.isNewSynchronization()) {
            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            // 清除 ThreadLocal
            TransactionSynchronizationManager.clearSynchronization();
            //如果没有事务 或者 新开了一个事务
            if (!status.hasTransaction() || status.isNewTransaction()) {
                TransactionSynchronizationUtils.invokeAfterCompletion(synchronizations, completionStatus);
            }
            //如果 事务同步 不为空
            else if (!synchronizations.isEmpty()) {
                TransactionSynchronizationUtils.invokeAfterCompletion(synchronizations, TransactionSynchronization.STATUS_UNKNOWN);
            }
        }
    }

    private void cleanupAfterCompletion(DefaultTransactionStatus status) {
        //设置事务结束标识
        status.setCompleted();
        //清除ThreadLocal 内容
        if (status.isNewSynchronization()) {
            TransactionSynchronizationManager.clear();
        }
        if (status.isNewTransaction()) {
            doCleanupAfterCompletion(status.getTransaction());
        }
        if (status.getSuspendedResources() != null) {
            log.debug("Resuming suspended transaction after completion of inner transaction");
            Object transaction = status.hasTransaction() ? status.getTransaction() : null;
            // 恢复 事务 和 事务同步
            resume(transaction, (SuspendedResourcesHolder) status.getSuspendedResources());
        }
    }

    protected void doCleanupAfterCompletion(Object transaction) {
    }

    protected void doSetRollbackOnly(DefaultTransactionStatus status) {
        throw new IllegalStateException("Participating in existing transactions is not supported - when 'isExistingTransaction' " +
                "returns true, appropriate 'doSetRollbackOnly' behavior must be provided");
    }

    protected int determineTimeout(TransactionDefinition definition) {
        if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
            return definition.getTimeout();
        }
        return getDefaultTimeout();
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

    /**
     * 默认是 SYNCHRONIZATION_ALWAYS
     */
    public final int getTransactionSynchronization() {
        return this.transactionSynchronization;
    }

    protected abstract Object doGetTransaction();

    protected abstract void doBegin(Object transaction, TransactionDefinition definition);

    protected abstract void doCommit(DefaultTransactionStatus status);

    protected abstract void doRollback(DefaultTransactionStatus status);

    /*--------------------------------------------------------------------------------------------------------------*/

    protected static final class SuspendedResourcesHolder {

        private final Object suspendedResources;

        private List<TransactionSynchronization> suspendedSynchronizations;

        private boolean readOnly;

        private Integer isolationLevel;

        private boolean wasActive;

        private SuspendedResourcesHolder(Object suspendedResources) {
            this.suspendedResources = suspendedResources;
        }

        private SuspendedResourcesHolder(
                Object suspendedResources, List<TransactionSynchronization> suspendedSynchronizations,
                boolean readOnly, Integer isolationLevel, boolean wasActive) {

            this.suspendedResources = suspendedResources;
            this.suspendedSynchronizations = suspendedSynchronizations;
            this.readOnly = readOnly;
            this.isolationLevel = isolationLevel;
            this.wasActive = wasActive;
        }
    }
}
