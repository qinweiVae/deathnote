package com.qinwei.deathnote.aop.autoproxy;

import com.qinwei.deathnote.aop.SpringProxy;
import com.qinwei.deathnote.aop.support.AdvisedSupport;

import java.lang.reflect.Proxy;

/**
 * @author qinwei
 * @date 2019-06-28
 */
public class DefaultAopProxyFactory implements AopProxyFactory {

    @Override
    public AopProxy createAopProxy(AdvisedSupport config) {
        if (config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
            Class<?> targetClass = config.getTargetClass();
            if (targetClass == null) {
                throw new IllegalStateException("TargetSource cannot determine target class: Either an interface or a target is required for proxy creation.");
            }
            // 如果是接口或者是proxy代理类， 创建 JDK 代理
            if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
                return new JdkDynamicAopProxy(config);
            }
            // 创建 CGLIB 代理
            return new CglibAopProxy(config);
        }
        //创建 JDK 代理
        return new JdkDynamicAopProxy(config);
    }

    /**
     * 判断AdvisedSupport 是否没有 可以代理的接口 或者 代理接口中只有 SpringProxy
     */
    private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
        Class<?>[] ifcs = config.getProxiedInterfaces();
        return (ifcs.length == 0 || (ifcs.length == 1 && SpringProxy.class.isAssignableFrom(ifcs[0])));
    }
}
