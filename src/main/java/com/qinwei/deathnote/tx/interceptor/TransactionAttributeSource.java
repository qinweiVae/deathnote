package com.qinwei.deathnote.tx.interceptor;

import java.lang.reflect.Method;

/**
 * @author qinwei
 * @date 2019-07-31
 */
public interface TransactionAttributeSource {

    TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass);
}
