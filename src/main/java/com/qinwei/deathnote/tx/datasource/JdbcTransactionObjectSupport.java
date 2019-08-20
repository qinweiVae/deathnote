package com.qinwei.deathnote.tx.datasource;

import com.qinwei.deathnote.tx.transaction.SavepointManager;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * @author qinwei
 * @date 2019-08-14
 */
@Slf4j
public abstract class JdbcTransactionObjectSupport implements SavepointManager {

    private ConnectionHolder connectionHolder;

    private Integer previousIsolationLevel;

    private boolean savepointAllowed = false;

    @Override
    public Object createSavepoint() {
        ConnectionHolder conHolder = getConnectionHolderForSavepoint();
        try {
            if (!conHolder.supportsSavepoints()) {
                throw new UnsupportedOperationException("Cannot create a nested transaction because savepoints are not supported by your JDBC driver");
            }
            if (conHolder.isRollbackOnly()) {
                throw new UnsupportedOperationException("Cannot create savepoint for transaction which is already marked as rollback-only");
            }
            return conHolder.createSavepoint();
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not create JDBC savepoint", ex);
        }
    }

    @Override
    public void rollbackToSavepoint(Object savepoint) {
        ConnectionHolder conHolder = getConnectionHolderForSavepoint();
        try {
            conHolder.getConnection().rollback((Savepoint) savepoint);
            conHolder.resetRollbackOnly();
        } catch (Throwable ex) {
            throw new IllegalStateException("Could not roll back to JDBC savepoint", ex);
        }
    }

    @Override
    public void releaseSavepoint(Object savepoint) {
        ConnectionHolder conHolder = getConnectionHolderForSavepoint();
        try {
            conHolder.getConnection().releaseSavepoint((Savepoint) savepoint);
        } catch (Throwable ex) {
            log.debug("Could not explicitly release JDBC savepoint", ex);
        }
    }

    protected ConnectionHolder getConnectionHolderForSavepoint() {
        if (!isSavepointAllowed()) {
            throw new UnsupportedOperationException(
                    "Transaction manager does not allow nested transactions");
        }
        if (!hasConnectionHolder()) {
            throw new UnsupportedOperationException("Cannot create nested transaction when not exposing a JDBC transaction");
        }
        return getConnectionHolder();
    }

    public ConnectionHolder getConnectionHolder() {
        if (this.connectionHolder == null) {
            throw new IllegalStateException("No ConnectionHolder available");
        }
        return this.connectionHolder;
    }

    public void setConnectionHolder(ConnectionHolder connectionHolder) {
        this.connectionHolder = connectionHolder;
    }

    public boolean hasConnectionHolder() {
        return this.connectionHolder != null;
    }

    public Integer getPreviousIsolationLevel() {
        return previousIsolationLevel;
    }

    public void setPreviousIsolationLevel(Integer previousIsolationLevel) {
        this.previousIsolationLevel = previousIsolationLevel;
    }

    public boolean isSavepointAllowed() {
        return savepointAllowed;
    }

    public void setSavepointAllowed(boolean savepointAllowed) {
        this.savepointAllowed = savepointAllowed;
    }
}
