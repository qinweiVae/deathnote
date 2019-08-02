package com.qinwei.deathnote.tx.transaction;

/**
 * @author qinwei
 * @date 2019-07-30
 */
public interface PlatformTransactionManager {

    TransactionStatus getTransaction(TransactionDefinition definition);

    void commit(TransactionStatus status);

    void rollback(TransactionStatus status);
}
