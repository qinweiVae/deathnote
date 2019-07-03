package com.qinwei.deathnote.aop.autoproxy;

import com.qinwei.deathnote.aop.support.AdvisedSupport;
import com.qinwei.deathnote.aop.targetSource.EmptyTargetSource;
import com.qinwei.deathnote.utils.ClassUtils;

import java.io.Serializable;

/**
 * @author qinwei
 * @date 2019-07-01
 */
class CglibAopProxy implements AopProxy, Serializable {

    private static final long serialVersionUID = -6334798172597262590L;

    private final AdvisedSupport advised;

    public CglibAopProxy(AdvisedSupport config) {
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
}
