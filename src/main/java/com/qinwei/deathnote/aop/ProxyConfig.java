package com.qinwei.deathnote.aop;

import java.io.Serializable;

/**
 * @author qinwei
 * @date 2019-06-28
 */
public class ProxyConfig implements Serializable {

    private static final long serialVersionUID = -3464248454600516593L;

    private boolean proxyTargetClass = false;

    boolean exposeProxy = false;


    public void setProxyTargetClass(boolean proxyTargetClass) {
        this.proxyTargetClass = proxyTargetClass;
    }

    public boolean isProxyTargetClass() {
        return this.proxyTargetClass;
    }

    public void setExposeProxy(boolean exposeProxy) {
        this.exposeProxy = exposeProxy;
    }

    public boolean isExposeProxy() {
        return this.exposeProxy;
    }

    public void copyFrom(ProxyConfig other) {
        this.proxyTargetClass = other.proxyTargetClass;
        this.exposeProxy = other.exposeProxy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("proxyTargetClass=").append(this.proxyTargetClass).append("; ");
        sb.append("exposeProxy=").append(this.exposeProxy).append("; ");
        return sb.toString();
    }

}
