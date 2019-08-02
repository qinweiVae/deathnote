package com.qinwei.deathnote.tx.interceptor;

import com.qinwei.deathnote.tx.transaction.TransactionDefinition;

/**
 * @author qinwei
 * @date 2019-07-31
 */
public interface TransactionAttribute extends TransactionDefinition {

    String getQualifier();

    boolean rollbackOn(Throwable ex);
}
