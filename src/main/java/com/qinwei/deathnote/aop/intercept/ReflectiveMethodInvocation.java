package com.qinwei.deathnote.aop.intercept;

import com.qinwei.deathnote.aop.support.AopUtils;
import com.qinwei.deathnote.context.support.BridgeMethodResolver;
import com.qinwei.deathnote.utils.ClassUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-07-12
 */
public class ReflectiveMethodInvocation implements MethodInvocation, Cloneable {

    protected final Object proxy;

    protected final Object target;

    protected final Method method;

    protected Object[] arguments = new Object[0];

    private final Class<?> targetClass;

    private Map<String, Object> userAttributes;

    protected final List<?> interceptorsAndDynamicMethodMatchers;

    private int currentInterceptorIndex = -1;

    public ReflectiveMethodInvocation(Object proxy, Object target, Method method, Object[] arguments,
                                      Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers) {
        this.proxy = proxy;
        this.target = target;
        this.targetClass = targetClass;
        this.method = BridgeMethodResolver.findBridgedMethod(method);
        this.arguments = AopUtils.adaptArgumentsIfNecessary(method, arguments);
        this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
    }

    @Override
    public Object proceed() throws Throwable {
        //如果拦截器链为空，或者已经执行完了拦截器
        if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
            //直接反射调用目标对象的方法
            ClassUtils.makeAccessible(this.method);
            return this.method.invoke(this.target, this.arguments);
        }
        // 获取下一个要执行的拦截器
        Object interceptorOrInterceptionAdvice = this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
        //MethodMatcher.isRuntime() 为 true 的时候，会把 MethodInterceptor 封装成 InterceptorAndDynamicMethodMatcher
        if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher) {
            InterceptorAndDynamicMethodMatcher dm = (InterceptorAndDynamicMethodMatcher) interceptorOrInterceptionAdvice;
            Class<?> targetClass = (this.targetClass != null ? this.targetClass : this.method.getDeclaringClass());
            if (dm.methodMatcher.matches(this.method, targetClass, this.arguments)) {
                return dm.interceptor.invoke(this);
            } else {
                return proceed();
            }
        } else {
            return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
        }
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public Object getProxy() {
        return this.proxy;
    }

    @Override
    public void setArguments(Object... arguments) {
        this.arguments = arguments;
    }

    @Override
    public Object[] getArguments() {
        return this.arguments;
    }

    @Override
    public void setUserAttribute(String key, Object value) {
        if (value != null) {
            if (this.userAttributes == null) {
                this.userAttributes = new HashMap<>();
            }
            this.userAttributes.put(key, value);
        } else {
            if (this.userAttributes != null) {
                this.userAttributes.remove(key);
            }
        }
    }

    @Override
    public Object getUserAttribute(String key) {
        return this.userAttributes != null ? this.userAttributes.get(key) : null;
    }

    @Override
    public MethodInvocation invocableClone() {
        Object[] cloneArguments = this.arguments;
        if (this.arguments.length > 0) {
            cloneArguments = new Object[this.arguments.length];
            System.arraycopy(this.arguments, 0, cloneArguments, 0, this.arguments.length);
        }
        return invocableClone(cloneArguments);
    }

    @Override
    public MethodInvocation invocableClone(Object... arguments) {
        if (this.userAttributes == null) {
            this.userAttributes = new HashMap<>();
        }
        try {
            ReflectiveMethodInvocation clone = (ReflectiveMethodInvocation) clone();
            clone.arguments = arguments;
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new IllegalStateException("Should be able to clone object of type [" + getClass() + "]: " + ex);
        }
    }

    @Override
    public Object getThis() {
        return this.target;
    }

    @Override
    public AccessibleObject getStaticPart() {
        return this.method;
    }
}
