package com.qinwei.deathnote.tx.support;

import com.qinwei.deathnote.tx.transaction.SavepointManager;

/**
 * @author qinwei
 * @date 2019-08-02
 */
public class DefaultTransactionStatus extends AbstractTransactionStatus {

    private final Object transaction;

    private final boolean newTransaction;

    private final boolean newSynchronization;

    private final boolean readOnly;

    private final Object suspendedResources;

    public DefaultTransactionStatus(
            Object transaction, boolean newTransaction, boolean newSynchronization,
            boolean readOnly, Object suspendedResources) {

        this.transaction = transaction;
        this.newTransaction = newTransaction;
        this.newSynchronization = newSynchronization;
        this.readOnly = readOnly;
        this.suspendedResources = suspendedResources;
    }

    @Override
    public boolean isNewTransaction() {
        return hasTransaction() && this.newTransaction;
    }

    public boolean hasTransaction() {
        return this.transaction != null;
    }

    public Object getTransaction() {
        return this.transaction;
    }

    public boolean isNewSynchronization() {
        return this.newSynchronization;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public Object getSuspendedResources() {
        return this.suspendedResources;
    }

    @Override
    protected SavepointManager getSavepointManager() {
        Object transaction = this.transaction;
        if (!(transaction instanceof SavepointManager)) {
            throw new UnsupportedOperationException("Transaction object [" + this.transaction + "] does not support savepoints");
        }
        return (SavepointManager) transaction;
    }
}
