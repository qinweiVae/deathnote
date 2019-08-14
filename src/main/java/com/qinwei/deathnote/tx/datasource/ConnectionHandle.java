package com.qinwei.deathnote.tx.datasource;

import java.sql.Connection;

/**
 * @author qinwei
 * @date 2019-08-14
 */
public interface ConnectionHandle {

    Connection getConnection();

    default void releaseConnection(Connection con) {
    }
}
