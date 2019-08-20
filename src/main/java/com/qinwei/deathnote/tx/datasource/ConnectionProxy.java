package com.qinwei.deathnote.tx.datasource;

import java.sql.Connection;

/**
 * @author qinwei
 * @date 2019-08-19 11:21
 * JdbcTemplate 使用jdk 动态代理创建的 代理类
 */
public interface ConnectionProxy extends Connection {

    Connection getTargetConnection();
}
