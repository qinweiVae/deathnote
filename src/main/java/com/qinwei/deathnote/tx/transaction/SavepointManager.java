package com.qinwei.deathnote.tx.transaction;

/**
 * @author qinwei
 * @date 2019-07-30
 */
public interface SavepointManager {

    Object createSavepoint();

    void rollbackToSavepoint(Object savepoint);

    void releaseSavepoint(Object savepoint);
}
