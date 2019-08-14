package com.qinwei.deathnote.tx.datasource;

import com.qinwei.deathnote.tx.support.ResourceHolderSupport;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * @author qinwei
 * @date 2019-08-14
 */
public class ConnectionHolder extends ResourceHolderSupport {

    public static final String SAVEPOINT_NAME_PREFIX = "SAVEPOINT_";

    private ConnectionHandle connectionHandle;

    private Connection currentConnection;

    private boolean transactionActive = false;

    private Boolean savepointsSupported;

    private int savepointCounter = 0;

    public ConnectionHolder(Connection connection) {
        this.connectionHandle = new SimpleConnectionHandle(connection);
    }

    public Connection getConnection() {
        if (this.connectionHandle == null) {
            throw new IllegalStateException("Active Connection is required");
        }
        if (this.currentConnection == null) {
            this.currentConnection = this.connectionHandle.getConnection();
        }
        return this.currentConnection;
    }

    protected void setConnection(Connection connection) {
        if (this.currentConnection != null) {
            if (this.connectionHandle != null) {
                this.connectionHandle.releaseConnection(this.currentConnection);
            }
            this.currentConnection = null;
        }
        if (connection != null) {
            this.connectionHandle = new SimpleConnectionHandle(connection);
        } else {
            this.connectionHandle = null;
        }
    }

    public boolean supportsSavepoints() throws SQLException {
        if (this.savepointsSupported == null) {
            this.savepointsSupported = getConnection().getMetaData().supportsSavepoints();
        }
        return this.savepointsSupported;
    }

    public Savepoint createSavepoint() throws SQLException {
        this.savepointCounter++;
        return getConnection().setSavepoint(SAVEPOINT_NAME_PREFIX + this.savepointCounter);
    }

    protected boolean hasConnection() {
        return this.connectionHandle != null;
    }

    protected void setTransactionActive(boolean transactionActive) {
        this.transactionActive = transactionActive;
    }

    protected boolean isTransactionActive() {
        return this.transactionActive;
    }


    @Override
    public void released() {
        super.released();
        if (!isOpen() && this.currentConnection != null) {
            if (this.connectionHandle != null) {
                this.connectionHandle.releaseConnection(this.currentConnection);
            }
            this.currentConnection = null;
        }
    }

    @Override
    public void clear() {
        super.clear();
        this.transactionActive = false;
        this.savepointsSupported = null;
        this.savepointCounter = 0;
    }
}
