package com.qinwei.deathnote.tx.datasource;

import com.qinwei.deathnote.tx.support.TransactionSynchronization;
import com.qinwei.deathnote.tx.support.TransactionSynchronizationManager;
import com.qinwei.deathnote.tx.transaction.TransactionDefinition;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @Author qinwei
 * @Date 2019-08-19 11:13
 * @Description
 */
@Slf4j
public abstract class DataSourceUtils {

    public static Connection getConnection(DataSource dataSource) {
        try {
            return doGetConnection(dataSource);
        } catch (Throwable ex) {
            throw new RuntimeException("Failed to obtain JDBC Connection: " + ex.getMessage());
        }
    }

    public static Connection doGetConnection(DataSource dataSource) throws SQLException {
        assert dataSource != null : "No DataSource specified";
        //如果 ThreadLocal 里面有
        ConnectionHolder conHolder = TransactionSynchronizationManager.getResource(dataSource);
        if (conHolder != null && (conHolder.hasConnection() || conHolder.isSynchronizedWithTransaction())) {
            conHolder.requested();
            if (!conHolder.hasConnection()) {
                log.debug("Fetching resumed JDBC Connection from DataSource");
                conHolder.setConnection(fetchConnection(dataSource));
            }
            return conHolder.getConnection();
        }
        log.debug("Fetching JDBC Connection from DataSource");
        Connection con = fetchConnection(dataSource);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            try {
                ConnectionHolder holderToUse = conHolder;
                if (holderToUse == null) {
                    holderToUse = new ConnectionHolder(con);
                } else {
                    holderToUse.setConnection(con);
                }
                holderToUse.requested();
                TransactionSynchronizationManager.registerSynchronization(new ConnectionSynchronization(holderToUse, dataSource));
                holderToUse.setSynchronizedWithTransaction(true);
                if (holderToUse != conHolder) {
                    TransactionSynchronizationManager.bindResource(dataSource, holderToUse);
                }
            } catch (RuntimeException ex) {
                releaseConnection(con, dataSource);
                throw ex;
            }
        }
        return con;
    }

    private static Connection fetchConnection(DataSource dataSource) throws SQLException {
        Connection con = dataSource.getConnection();
        if (con == null) {
            throw new IllegalStateException("DataSource returned null from getConnection(): " + dataSource);
        }
        return con;
    }

    public static void releaseConnection(Connection con, DataSource dataSource) {
        try {
            doReleaseConnection(con, dataSource);
        } catch (SQLException ex) {
            log.debug("Could not close JDBC Connection", ex);
        } catch (Throwable ex) {
            log.debug("Unexpected exception on closing JDBC Connection", ex);
        }
    }

    private static void doReleaseConnection(Connection con, DataSource dataSource) throws SQLException {
        if (con == null) {
            return;
        }
        if (dataSource != null) {
            ConnectionHolder conHolder = TransactionSynchronizationManager.getResource(dataSource);
            if (conHolder != null && connectionEquals(conHolder, con)) {
                conHolder.released();
                return;
            }
        }
        con.close();
    }

    private static boolean connectionEquals(ConnectionHolder conHolder, Connection con) {
        if (!conHolder.hasConnection()) {
            return false;
        }
        Connection heldCon = conHolder.getConnection();
        return heldCon == con || heldCon.equals(con) || getTargetConnection(heldCon).equals(con);
    }

    public static Object getTargetConnection(Connection con) {
        Connection conToUse = con;
        while (conToUse instanceof ConnectionProxy) {
            conToUse = ((ConnectionProxy) conToUse).getTargetConnection();
        }
        return conToUse;
    }

    /**
     * 设置readonly 为 true , 并设置 隔离级别
     */
    public static Integer prepareConnectionForTransaction(Connection con, TransactionDefinition definition) throws SQLException {
        assert con != null : "No Connection specified";
        // 设置 readonly
        if (definition != null && definition.isReadOnly()) {
            try {
                log.debug("Setting JDBC Connection [" + con + "] read-only");
                con.setReadOnly(true);
            } catch (SQLException | RuntimeException ex) {
                Throwable exToCheck = ex;
                while (exToCheck != null) {
                    if (exToCheck.getClass().getSimpleName().contains("Timeout")) {
                        throw ex;
                    }
                    exToCheck = exToCheck.getCause();
                }
                log.debug("Could not set JDBC Connection read-only", ex);
            }
        }

        Integer previousIsolationLevel = null;
        // 如果配置的不是默认的隔离级别
        if (definition != null && definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
            log.debug("Changing isolation level of JDBC Connection [" + con + "] to " + definition.getIsolationLevel());
            int currentIsolation = con.getTransactionIsolation();
            if (currentIsolation != definition.getIsolationLevel()) {
                previousIsolationLevel = currentIsolation;
                con.setTransactionIsolation(definition.getIsolationLevel());
            }
        }
        return previousIsolationLevel;
    }

    /**
     * 设置readonly 为 false , 并还原 隔离级别
     */
    public static void resetConnectionAfterTransaction(Connection con, Integer previousIsolationLevel) {
        assert con != null : "No Connection specified";
        try {
            if (previousIsolationLevel != null) {
                log.debug("Resetting isolation level of JDBC Connection [" + con + "] to " + previousIsolationLevel);
                con.setTransactionIsolation(previousIsolationLevel);
            }
            if (con.isReadOnly()) {
                log.debug("Resetting read-only flag of JDBC Connection [" + con + "]");
                con.setReadOnly(false);
            }
        } catch (Throwable ex) {
            log.debug("Could not reset JDBC Connection after transaction", ex);
        }
    }


    private static class ConnectionSynchronization implements TransactionSynchronization {

        private final ConnectionHolder connectionHolder;

        private final DataSource dataSource;

        private boolean holderActive = true;

        public ConnectionSynchronization(ConnectionHolder connectionHolder, DataSource dataSource) {
            this.connectionHolder = connectionHolder;
            this.dataSource = dataSource;
        }

        @Override
        public void suspend() {
            if (this.holderActive) {
                TransactionSynchronizationManager.unbindResource(this.dataSource);
                if (this.connectionHolder.hasConnection() && !this.connectionHolder.isOpen()) {
                    releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
                    this.connectionHolder.setConnection(null);
                }
            }
        }

        @Override
        public void resume() {
            if (this.holderActive) {
                TransactionSynchronizationManager.bindResource(this.dataSource, this.connectionHolder);
            }
        }

        @Override
        public void beforeCompletion() {
            if (!this.connectionHolder.isOpen()) {
                TransactionSynchronizationManager.unbindResource(this.dataSource);
                this.holderActive = false;
                if (this.connectionHolder.hasConnection()) {
                    releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
                }
            }
        }

        @Override
        public void afterCompletion(int status) {
            if (this.holderActive) {
                TransactionSynchronizationManager.doUnbindResource(this.dataSource);
                this.holderActive = false;
                if (this.connectionHolder.hasConnection()) {
                    releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
                    this.connectionHolder.setConnection(null);
                }
            }
            this.connectionHolder.reset();
        }
    }

}
