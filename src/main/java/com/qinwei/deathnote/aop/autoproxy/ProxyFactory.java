package com.qinwei.deathnote.aop.autoproxy;

import com.qinwei.deathnote.aop.AdvisedSupportListener;
import com.qinwei.deathnote.aop.support.AdvisedSupport;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public class ProxyFactory extends AdvisedSupport {

    private AopProxyFactory aopProxyFactory;

    private final List<AdvisedSupportListener> listeners = new LinkedList<>();

    private AtomicBoolean active = new AtomicBoolean(false);

    public ProxyFactory() {
        this.aopProxyFactory = new DefaultAopProxyFactory();
    }

    public Object getProxy() {
        return createAopProxy().getProxy();
    }

    public Object getProxy(ClassLoader classLoader) {
        return createAopProxy().getProxy(classLoader);
    }

    protected final AopProxy createAopProxy() {
        if (!this.active.get()) {
            activate();
        }
        return getAopProxyFactory().createAopProxy(this);
    }

    public void setAopProxyFactory(AopProxyFactory aopProxyFactory) {
        this.aopProxyFactory = aopProxyFactory;
    }

    public AopProxyFactory getAopProxyFactory() {
        return this.aopProxyFactory;
    }

    public void addListener(AdvisedSupportListener listener) {
        assert listener != null : "AdvisedSupportListener must not be null";
        this.listeners.add(listener);
    }

    public void removeListener(AdvisedSupportListener listener) {
        assert listener != null : "AdvisedSupportListener must not be null";
        this.listeners.remove(listener);
    }

    private void activate() {
        if (this.active.compareAndSet(false, true)) {
            for (AdvisedSupportListener listener : this.listeners) {
                listener.activated(this);
            }
        }
        throw new IllegalStateException("Unable to activate proxy creator");
    }

    @Override
    protected void adviceChanged() {
        super.adviceChanged();
        if (this.active.get()) {
            for (AdvisedSupportListener listener : this.listeners) {
                listener.adviceChanged(this);
            }
        }
    }
}
