package com.qinwei.deathnote.aop.autoproxy;

import com.qinwei.deathnote.aop.Advised;
import com.qinwei.deathnote.aop.intercept.MethodInvocation;
import com.qinwei.deathnote.aop.intercept.ReflectiveMethodInvocation;
import com.qinwei.deathnote.aop.support.AdvisedSupport;
import com.qinwei.deathnote.aop.support.AopContext;
import com.qinwei.deathnote.aop.support.AopUtils;
import com.qinwei.deathnote.aop.targetSource.EmptyTargetSource;
import com.qinwei.deathnote.aop.targetSource.TargetSource;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @author qinwei
 * @date 2019-07-01
 */
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {

    private static final long serialVersionUID = -8286297516135454422L;

    private final AdvisedSupport advised;

    private boolean equalsDefined;

    private boolean hashCodeDefined;

    public JdkDynamicAopProxy(AdvisedSupport config) {
        assert config != null : "AdvisedSupport must not be null";
        if (config.getAdvisors().length == 0 && config.getTargetSource() == EmptyTargetSource.INSTANCE) {
            throw new IllegalStateException("No advisors and no TargetSource specified");
        }
        this.advised = config;
    }

    @Override
    public Object getProxy() {
        return getProxy(ClassUtils.getDefaultClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        // 得到代理接口
        Class<?>[] proxiedInterfaces = AopUtils.completeProxiedInterfaces(this.advised);
        // 处理 equals , hashcode 方法
        findDefinedEqualsAndHashCodeMethods(proxiedInterfaces);
        // 创建代理
        return Proxy.newProxyInstance(classLoader, proxiedInterfaces, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object oldProxy = null;
        boolean setProxyContext = false;

        TargetSource targetSource = this.advised.getTargetSource();
        Object target = null;
        try {
            // 如果是 equals 方法，不对equal方法进行AOP拦截
            if (!this.equalsDefined && ClassUtils.isEqualsMethod(method)) {
                return equals(args[0]);
            }
            // 如果是 hashCode 方法，不对hashCode方法进行AOP拦截
            if (!this.hashCodeDefined && ClassUtils.isHashCodeMethod(method)) {
                return hashCode();
            }
            // 如果被代理的目标对象实现了Advised接口，直接用反射执行目标对象的方法。不做增强处理。
            if (method.getDeclaringClass().isInterface() && ClassUtils.isAssignable(Advised.class, method.getDeclaringClass())) {
                ClassUtils.makeAccessible(method);
                return method.invoke(this.advised, args);
            }

            // 这里对应的是 expose-proxy 属性的应用，把代理暴露处理
            // 目标方法内部的自我调用将无法实施切面中的增强，所以在这里需要把代理暴露出去
            if (this.advised.isExposeProxy()) {
                // 向 AopContext 中设置代理对象
                oldProxy = AopContext.setCurrentProxy(proxy);
                setProxyContext = true;
            }

            // 得到目标对象 (就是 bean)
            target = targetSource.getTarget();
            Class<?> targetClass = target != null ? target.getClass() : null;

            Object retVal;

            // 获取该方法对应的拦截器链。
            List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
            // 如果没有任何拦截器,则通过反射调用对应方法。
            if (CollectionUtils.isEmpty(chain)) {
                //解析method 的可变参数
                Object[] argsToUse = AopUtils.adaptArgumentsIfNecessary(method, args);
                ClassUtils.makeAccessible(method);
                retVal = method.invoke(target, argsToUse);
            } else {
                // 把所有的拦截器封装在ReflectiveMethodInvocation中，以便于链式调用
                MethodInvocation invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
                // 执行拦截器链
                retVal = invocation.proceed();
            }
            Class<?> returnType = method.getReturnType();
            // 如果返回的是this,则判断是否需要把retVal设置为代理对象。
            if (retVal != null && retVal == target && returnType != Object.class && returnType.isInstance(proxy)) {
                retVal = proxy;
            } else if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
                throw new IllegalStateException("Null return value from advice does not match primitive return type for: " + method);
            }
            return retVal;
        } finally {
            if (target != null && !targetSource.isStatic()) {
                targetSource.releaseTarget(target);
            }
            if (setProxyContext) {
                AopContext.setCurrentProxy(oldProxy);
            }
        }
    }

    /**
     * 处理 equals , hashcode 方法
     */
    private void findDefinedEqualsAndHashCodeMethods(Class<?>[] proxiedInterfaces) {
        for (Class<?> proxiedInterface : proxiedInterfaces) {
            Method[] methods = proxiedInterface.getDeclaredMethods();
            for (Method method : methods) {
                if (ClassUtils.isEqualsMethod(method)) {
                    this.equalsDefined = true;
                }
                if (ClassUtils.isHashCodeMethod(method)) {
                    this.hashCodeDefined = true;
                }
                if (this.equalsDefined && this.hashCodeDefined) {
                    return;
                }
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }

        JdkDynamicAopProxy otherProxy;
        if (other instanceof JdkDynamicAopProxy) {
            otherProxy = (JdkDynamicAopProxy) other;
        } else if (Proxy.isProxyClass(other.getClass())) {
            InvocationHandler ih = Proxy.getInvocationHandler(other);
            if (!(ih instanceof JdkDynamicAopProxy)) {
                return false;
            }
            otherProxy = (JdkDynamicAopProxy) ih;
        } else {
            return false;
        }
        return AopUtils.equalsInProxy(this.advised, otherProxy.advised);
    }

    @Override
    public int hashCode() {
        return JdkDynamicAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
    }
}
