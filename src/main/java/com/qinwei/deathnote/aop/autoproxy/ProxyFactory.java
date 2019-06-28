package com.qinwei.deathnote.aop.autoproxy;

import com.qinwei.deathnote.aop.support.ProxyCreatorSupport;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public class ProxyFactory extends ProxyCreatorSupport {

    public ProxyFactory() {
    }

    public Object getProxy() {
        return createAopProxy().getProxy();
    }

    public Object getProxy(ClassLoader classLoader) {
        return createAopProxy().getProxy(classLoader);
    }
}
