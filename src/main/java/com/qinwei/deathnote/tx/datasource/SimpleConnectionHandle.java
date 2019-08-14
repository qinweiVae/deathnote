package com.qinwei.deathnote.tx.datasource;

import java.sql.Connection;

/**
 * @author qinwei
 * @date 2019-08-14
 */
public class SimpleConnectionHandle implements ConnectionHandle {

    private final Connection connection;

    public SimpleConnectionHandle(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection must not be null");
        }
        this.connection = connection;
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public String toString() {
        return "SimpleConnectionHandle: " + this.connection;
    }
}
