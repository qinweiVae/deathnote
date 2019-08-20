package com.qinwei.deathnote.tx.datasource;

import com.qinwei.deathnote.beans.bean.InitializingBean;
import com.qinwei.deathnote.tx.support.DefaultTransactionStatus;
import com.qinwei.deathnote.tx.support.TransactionSynchronizationManager;
import com.qinwei.deathnote.tx.transaction.AbstractPlatformTransactionManager;
import com.qinwei.deathnote.tx.transaction.PlatformTransactionManager;
import com.qinwei.deathnote.tx.transaction.TransactionDefinition;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author qinwei
 * @date 2019-08-14
 */
@Slf4j
public class DataSourceTransactionManager extends AbstractPlatformTransactionManager implements PlatformTransactionManager, InitializingBean {

    private DataSource dataSource;

    private boolean enforceReadOnly = false;

    /**
     * 支持嵌套事务
     */
    public DataSourceTransactionManager() {
        setNestedTransactionAllowed(true);
    }

    protected DataSource obtainDataSource() {
        DataSource dataSource = getDataSource();
        if (dataSource == null) {
            throw new IllegalStateException("No DataSource set");
        }
        return dataSource;
    }

    @Override
    public void afterPropertiesSet() {
        if (getDataSource() == null) {
            throw new IllegalArgumentException("Property 'dataSource' is required");
        }
    }

    @Override
    protected Object doGetTransaction() {
        DataSourceTransactionObject txObject = new DataSourceTransactionObject();
        // DataSourceTransactionManager 中为 true
        txObject.setSavepointAllowed(isNestedTransactionAllowed());
        // 从ThreadLocal 中 取值
        ConnectionHolder conHolder = TransactionSynchronizationManager.getResource(obtainDataSource());
        txObject.setConnectionHolder(conHolder, false);
        return txObject;
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
        return txObject.hasConnectionHolder() && txObject.getConnectionHolder().isTransactionActive();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
        Connection con = null;
        try {
            // 如果 connectionHolder 为 null 或者 synchronizedWithTransaction 为 true
            if (!txObject.hasConnectionHolder() || txObject.getConnectionHolder().isSynchronizedWithTransaction()) {
                Connection newCon = obtainDataSource().getConnection();
                log.debug("Acquired Connection [" + newCon + "] for JDBC transaction");
                // 重新设置 connectionHolder
                txObject.setConnectionHolder(new ConnectionHolder(newCon), true);
            }
            txObject.getConnectionHolder().setSynchronizedWithTransaction(true);
            //拿到新的 Connection
            con = txObject.getConnectionHolder().getConnection();
            // 设置readonly 为 true , 并设置 隔离级别
            Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
            txObject.setPreviousIsolationLevel(previousIsolationLevel);
            // 改为手动 commit
            if (con.getAutoCommit()) {
                txObject.setMustRestoreAutoCommit(true);
                log.debug("Switching JDBC Connection [" + con + "] to manual commit");
                con.setAutoCommit(false);
            }
            //通过执行 sql 来 设置 readonly
            prepareTransactionalConnection(con, definition);
            // 设置 事务开启 标识
            txObject.getConnectionHolder().setTransactionActive(true);
            // 设置 超时 时间
            int timeout = determineTimeout(definition);
            if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
                txObject.getConnectionHolder().setTimeoutInSeconds(timeout);
            }
            // 把 Connection 绑定到 ThreadLocal 里面
            if (txObject.isNewConnectionHolder()) {
                TransactionSynchronizationManager.bindResource(obtainDataSource(), txObject.getConnectionHolder());
            }
        } catch (Throwable ex) {
            if (txObject.isNewConnectionHolder()) {
                // 释放连接
                DataSourceUtils.releaseConnection(con, obtainDataSource());
                txObject.setConnectionHolder(null, false);
            }
            throw new IllegalStateException("Could not open JDBC Connection for transaction");
        }
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
        Connection con = txObject.getConnectionHolder().getConnection();
        log.debug("Committing JDBC transaction on Connection [" + con + "]");
        try {
            //提交事务
            con.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not commit JDBC transaction", ex);
        }
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
        Connection con = txObject.getConnectionHolder().getConnection();
        log.debug("Rolling back JDBC transaction on Connection [" + con + "]");
        try {
            //回滚事务
            con.rollback();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not roll back JDBC transaction", ex);
        }
    }

    @Override
    protected Object doSuspend(Object transaction) {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
        txObject.setConnectionHolder(null);
        return TransactionSynchronizationManager.unbindResource(obtainDataSource());
    }

    @Override
    protected void doResume(Object transaction, Object suspendedResources) {
        TransactionSynchronizationManager.bindResource(obtainDataSource(), (ConnectionHolder) suspendedResources);
    }

    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
        log.debug("Setting JDBC transaction [" + txObject.getConnectionHolder().getConnection() + "] rollback-only");
        txObject.setRollbackOnly();
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
        //如果存在事务 , 清除 ThreadLocal
        if (txObject.isNewConnectionHolder()) {
            TransactionSynchronizationManager.unbindResource(obtainDataSource());
        }
        // 重置 Connection
        Connection con = txObject.getConnectionHolder().getConnection();
        try {
            if (txObject.isMustRestoreAutoCommit()) {
                con.setAutoCommit(true);
            }
            //设置readonly 为 false , 并还原 隔离级别
            DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel());
        } catch (Throwable ex) {
            log.debug("Could not reset JDBC Connection after transaction", ex);
        }

        if (txObject.isNewConnectionHolder()) {
            log.debug("Releasing JDBC Connection [" + con + "] after transaction");
            DataSourceUtils.releaseConnection(con, this.dataSource);
        }

        txObject.getConnectionHolder().clear();
    }

    protected void prepareTransactionalConnection(Connection con, TransactionDefinition definition) throws SQLException {
        // enforceReadOnly 默认是 false
        if (isEnforceReadOnly() && definition.isReadOnly()) {
            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate("SET TRANSACTION READ ONLY");
            }
        }
    }

    public boolean isEnforceReadOnly() {
        return this.enforceReadOnly;
    }

    public void setEnforceReadOnly(boolean enforceReadOnly) {
        this.enforceReadOnly = enforceReadOnly;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /*----------------------------------------------------------------------------------------------------------------*/

    private static class DataSourceTransactionObject extends JdbcTransactionObjectSupport {

        private boolean newConnectionHolder;

        private boolean mustRestoreAutoCommit;

        public void setConnectionHolder(ConnectionHolder connectionHolder, boolean newConnectionHolder) {
            super.setConnectionHolder(connectionHolder);
            this.newConnectionHolder = newConnectionHolder;
        }

        public boolean isNewConnectionHolder() {
            return this.newConnectionHolder;
        }

        public void setMustRestoreAutoCommit(boolean mustRestoreAutoCommit) {
            this.mustRestoreAutoCommit = mustRestoreAutoCommit;
        }

        public boolean isMustRestoreAutoCommit() {
            return this.mustRestoreAutoCommit;
        }

        public void setRollbackOnly() {
            getConnectionHolder().setRollbackOnly();
        }

    }

}
