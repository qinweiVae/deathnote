package com.qinwei.deathnote.tx.transaction;

import java.io.Flushable;

/**
 * @author qinwei
 * @date 2019-07-30
 */
public interface TransactionStatus extends SavepointManager, Flushable {

    boolean isNewTransaction();

    boolean hasSavepoint();

    void setRollbackOnly();

    boolean isRollbackOnly();

    @Override
    void flush();

    boolean isCompleted();
}
