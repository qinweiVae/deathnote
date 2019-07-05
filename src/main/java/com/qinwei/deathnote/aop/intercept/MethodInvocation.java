package com.qinwei.deathnote.aop.intercept;

import java.lang.reflect.Method;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public interface MethodInvocation extends Invocation {

    Method getMethod();

    Object getProxy();

    void setArguments(Object... arguments);

    void setUserAttribute(String key, Object value);

    Object getUserAttribute(String key);

    MethodInvocation invocableClone();

    MethodInvocation invocableClone(Object... arguments);
}
