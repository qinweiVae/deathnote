package com.qinwei.deathnote.tx.transaction;

/**
 * @author qinwei
 * @date 2019-07-31
 */
public class DefaultTransactionDefinition implements TransactionDefinition {

    private int propagationBehavior = PROPAGATION_REQUIRED;

    private int isolationLevel = ISOLATION_DEFAULT;

    private int timeout = TIMEOUT_DEFAULT;

    private boolean readOnly = false;

    public DefaultTransactionDefinition() {
    }

    public DefaultTransactionDefinition(TransactionDefinition other) {
        this.propagationBehavior = other.getPropagationBehavior();
        this.isolationLevel = other.getIsolationLevel();
        this.timeout = other.getTimeout();
        this.readOnly = other.isReadOnly();
    }

    @Override
    public int getPropagationBehavior() {
        return this.propagationBehavior;
    }

    public final void setPropagationBehavior(int propagationBehavior) {
        this.propagationBehavior = propagationBehavior;
    }

    @Override
    public int getIsolationLevel() {
        return this.isolationLevel;
    }

    public final void setIsolationLevel(int isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    @Override
    public int getTimeout() {
        return this.timeout;
    }

    public final void setTimeout(int timeout) {
        if (timeout < TIMEOUT_DEFAULT) {
            throw new IllegalArgumentException("Timeout must be a positive integer or TIMEOUT_DEFAULT");
        }
        this.timeout = timeout;
    }

    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

    public final void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

}
