package com.qinwei.deathnote.aop.support;

/**
 * @author qinwei
 * @date 2019-07-10
 */
public final class AopContext {

    private static final ThreadLocal<Object> currentProxy = new ThreadLocal<>();

    private AopContext() {
    }

    public static Object currentProxy() throws IllegalStateException {
        Object proxy = currentProxy.get();
        if (proxy == null) {
            throw new IllegalStateException("Cannot find current proxy: Set 'exposeProxy' property on Advised to 'true' to make it available.");
        }
        return proxy;
    }

    public static Object setCurrentProxy(Object proxy) {
        Object old = currentProxy.get();
        if (proxy != null) {
            currentProxy.set(proxy);
        } else {
            currentProxy.remove();
        }
        return old;
    }
}
