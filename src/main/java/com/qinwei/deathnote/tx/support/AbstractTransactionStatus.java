package com.qinwei.deathnote.tx.support;

import com.qinwei.deathnote.tx.transaction.SavepointManager;
import com.qinwei.deathnote.tx.transaction.TransactionStatus;

/**
 * @author qinwei
 * @date 2019-08-02
 */
public abstract class AbstractTransactionStatus implements TransactionStatus {

    private boolean rollbackOnly = false;

    private boolean completed = false;

    private Object savepoint;

    @Override
    public boolean hasSavepoint() {
        return this.savepoint != null;
    }

    protected Object getSavepoint() {
        return this.savepoint;
    }

    protected void setSavepoint(Object savepoint) {
        this.savepoint = savepoint;
    }

    @Override
    public void setRollbackOnly() {
        this.rollbackOnly = true;
    }

    @Override
    public boolean isRollbackOnly() {
        return isLocalRollbackOnly();
    }

    public boolean isLocalRollbackOnly() {
        return this.rollbackOnly;
    }

    @Override
    public void flush() {

    }

    public void setCompleted() {
        this.completed = true;
    }

    @Override
    public boolean isCompleted() {
        return this.completed;
    }

    @Override
    public Object createSavepoint() {
        return getSavepointManager().createSavepoint();
    }

    @Override
    public void rollbackToSavepoint(Object savepoint) {
        getSavepointManager().rollbackToSavepoint(savepoint);
    }

    @Override
    public void releaseSavepoint(Object savepoint) {
        getSavepointManager().releaseSavepoint(savepoint);
    }

    public void createAndHoldSavepoint() {
        setSavepoint(getSavepointManager().createSavepoint());
    }

    public void rollbackToHeldSavepoint() {
        Object savepoint = getSavepoint();
        if (savepoint == null) {
            throw new IllegalStateException("Cannot roll back to savepoint - no savepoint associated with current transaction");
        }
        getSavepointManager().rollbackToSavepoint(savepoint);
        getSavepointManager().releaseSavepoint(savepoint);
        setSavepoint(null);
    }

    protected SavepointManager getSavepointManager() {
        throw new UnsupportedOperationException("This transaction does not support savepoints");
    }

    public void releaseHeldSavepoint() {
        Object savepoint = getSavepoint();
        if (savepoint == null) {
            throw new UnsupportedOperationException("Cannot release savepoint - no savepoint associated with current transaction");
        }
        getSavepointManager().releaseSavepoint(savepoint);
        setSavepoint(null);
    }
}
