package com.qinwei.deathnote.tx.support;

import java.io.Flushable;

/**
 * @author qinwei
 * @date 2019-08-02
 */
public interface TransactionSynchronization extends Flushable {

    int STATUS_COMMITTED = 0;

    int STATUS_ROLLED_BACK = 1;

    int STATUS_UNKNOWN = 2;


    default void suspend() {
    }

    default void resume() {
    }

    @Override
    default void flush() {
    }

    default void beforeCommit(boolean readOnly) {
    }

    default void beforeCompletion() {
    }

    default void afterCommit() {
    }

    default void afterCompletion(int status) {
    }

}
