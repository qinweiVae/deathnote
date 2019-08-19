package com.qinwei.deathnote.tx.datasource;

import java.io.Flushable;

/**
 * @author qinwei
 * @date 2019-08-14
 */
public interface SmartTransactionObject extends Flushable {

    boolean isRollbackOnly();

    @Override
    void flush();
}
