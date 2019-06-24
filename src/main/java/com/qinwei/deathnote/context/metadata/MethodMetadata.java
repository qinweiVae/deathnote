package com.qinwei.deathnote.context.metadata;

import java.lang.reflect.Method;

/**
 * @author qinwei
 * @date 2019-06-14
 */
public interface MethodMetadata extends AnnotatedTypeMetadata {

    Method getIntrospectedMethod();

    String getMethodName();

    /**
     * 获取 当前method 所在的定义类
     */
    String getDeclaringClassName();

    String getReturnTypeName();

    boolean isAbstract();

    boolean isStatic();

    boolean isFinal();

    boolean isOverridable();

}
