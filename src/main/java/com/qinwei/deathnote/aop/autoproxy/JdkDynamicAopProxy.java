package com.qinwei.deathnote.aop.autoproxy;

import com.qinwei.deathnote.aop.support.AdvisedSupport;
import com.qinwei.deathnote.aop.support.AopUtils;
import com.qinwei.deathnote.aop.targetSource.EmptyTargetSource;
import com.qinwei.deathnote.utils.ClassUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
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
