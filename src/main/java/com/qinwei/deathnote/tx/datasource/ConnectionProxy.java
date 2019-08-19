package com.qinwei.deathnote.tx.datasource;

import java.sql.Connection;

/**
 * @Author qinwei
 * @Date 2019-08-19 11:21
 * @Description
 */
public interface ConnectionProxy extends Connection {

    Connection getTargetConnection();
}
