package com.qinwei.deathnote.tx.interceptor;

import com.qinwei.deathnote.aop.support.AopUtils;
import com.qinwei.deathnote.utils.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author qinwei
 * @date 2019-07-31
 */
public abstract class AbstractFallbackTransactionAttributeSource implements TransactionAttributeSource {

    @Override
    public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
        // 如果是 Object 里面的方法 直接返回
        if (method.getDeclaringClass() == Object.class) {
            return null;
        }
        // 拿到 方法或者 类 里面的 TransactionAttribute
        TransactionAttribute txAttr = computeTransactionAttribute(method, targetClass);
        //如果是 DefaultTransactionAttribute ,设置 descriptor (即 类名.方法名)
        if (txAttr instanceof DefaultTransactionAttribute) {
            ((DefaultTransactionAttribute) txAttr).setDescriptor(ClassUtils.getQualifiedMethodName(method, targetClass));
        }
        return txAttr;
    }

    private TransactionAttribute computeTransactionAttribute(Method method, Class<?> targetClass) {
        // 是否只支持 public 方法
        if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
            return null;
        }
        //解析 method ，得到真实的 method
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        //拿到 方法 关联的 事物属性
        TransactionAttribute txAttr = findTransactionAttribute(specificMethod);
        if (txAttr != null) {
            return txAttr;
        }
        //如果方法上没找到，再从类上找
        txAttr = findTransactionAttribute(specificMethod.getDeclaringClass());
        if (txAttr != null) {
            return txAttr;
        }
        //如果都没找到，再根据原始的method 去找
        if (specificMethod != method) {
            txAttr = findTransactionAttribute(method);
            if (txAttr != null) {
                return txAttr;
            }
            txAttr = findTransactionAttribute(method.getDeclaringClass());
            if (txAttr != null) {
                return txAttr;
            }
        }
        return null;
    }

    protected boolean allowPublicMethodsOnly() {
        return false;
    }

    protected abstract TransactionAttribute findTransactionAttribute(Class<?> clazz);

    protected abstract TransactionAttribute findTransactionAttribute(Method method);
}
