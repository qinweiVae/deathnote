package com.qinwei.deathnote.tx.annotation;

import com.qinwei.deathnote.tx.transaction.TransactionDefinition;

/**
 * @author qinwei
 * @date 2019-07-31
 */
public enum Isolation {

    DEFAULT(TransactionDefinition.ISOLATION_DEFAULT),

    READ_UNCOMMITTED(TransactionDefinition.ISOLATION_READ_UNCOMMITTED),

    READ_COMMITTED(TransactionDefinition.ISOLATION_READ_COMMITTED),

    REPEATABLE_READ(TransactionDefinition.ISOLATION_REPEATABLE_READ),

    SERIALIZABLE(TransactionDefinition.ISOLATION_SERIALIZABLE);


    private final int value;


    Isolation(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

}
