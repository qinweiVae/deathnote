package com.qinwei.deathnote.aop.adapter;

import com.qinwei.deathnote.aop.aspectj.AfterAdvice;
import com.qinwei.deathnote.aop.intercept.MethodInterceptor;
import com.qinwei.deathnote.aop.intercept.MethodInvocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-07-12
 */
public class ThrowsAdviceInterceptor implements MethodInterceptor, AfterAdvice {

    private static final String AFTER_THROWING = "afterThrowing";

    private final Object throwsAdvice;

    private final Map<Class<?>, Method> exceptionHandlerMap = new HashMap<>();

    /**
     * public void afterThrowing(Exception ex)
     * public void afterThrowing(RemoteException)
     * public void afterThrowing(Method method, Object[] args, Object target, Exception ex)
     * public void afterThrowing(Method method, Object[] args, Object target, ServletException ex)
     */
    public ThrowsAdviceInterceptor(Object throwsAdvice) {
        this.throwsAdvice = throwsAdvice;
        Method[] methods = throwsAdvice.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(AFTER_THROWING) &&
                    (method.getParameterCount() == 1 || method.getParameterCount() == 4)) {
                Class<?> throwableParam = method.getParameterTypes()[method.getParameterCount() - 1];
                if (Throwable.class.isAssignableFrom(throwableParam)) {
                    this.exceptionHandlerMap.put(throwableParam, method);
                }
            }
        }

        if (this.exceptionHandlerMap.isEmpty()) {
            throw new IllegalArgumentException("At least one handler method must be found in class [" + throwsAdvice.getClass() + "]");
        }
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        } catch (Throwable ex) {
            Method handlerMethod = getExceptionHandler(ex);
            if (handlerMethod != null) {
                invokeHandlerMethod(mi, ex, handlerMethod);
            }
            throw ex;
        }
    }

    private Method getExceptionHandler(Throwable exception) {
        Class<?> exceptionClass = exception.getClass();
        Method handler = this.exceptionHandlerMap.get(exceptionClass);
        while (handler == null && exceptionClass != Throwable.class) {
            exceptionClass = exceptionClass.getSuperclass();
            handler = this.exceptionHandlerMap.get(exceptionClass);
        }
        return handler;
    }

    private void invokeHandlerMethod(MethodInvocation mi, Throwable ex, Method method) throws Throwable {
        Object[] handlerArgs;
        if (method.getParameterCount() == 1) {
            handlerArgs = new Object[]{ex};
        } else {
            handlerArgs = new Object[]{mi.getMethod(), mi.getArguments(), mi.getThis(), ex};
        }
        try {
            method.invoke(this.throwsAdvice, handlerArgs);
        } catch (InvocationTargetException targetEx) {
            throw targetEx.getTargetException();
        }
    }

}
