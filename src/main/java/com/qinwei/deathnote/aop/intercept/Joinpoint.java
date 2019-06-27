package com.qinwei.deathnote.aop.intercept;

import java.lang.reflect.AccessibleObject;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public interface Joinpoint {

    Object proceed() throws Throwable;

    Object getThis();

    AccessibleObject getStaticPart();
}
