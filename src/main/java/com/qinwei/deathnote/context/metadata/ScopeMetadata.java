package com.qinwei.deathnote.context.metadata;

import com.qinwei.deathnote.context.annotation.ScopedProxyMode;

import static com.qinwei.deathnote.beans.factory.ConfigurableBeanFactory.SCOPE_SINGLETON;

/**
 * @author qinwei
 * @date 2019-06-17
 */
public class ScopeMetadata {

    private String scopeName = SCOPE_SINGLETON;

    private ScopedProxyMode scopedProxyMode = ScopedProxyMode.NO;


    public void setScopeName(String scopeName) {
        assert scopeName != null : "'scopeName' must not be null";
        this.scopeName = scopeName;
    }

    public String getScopeName() {
        return this.scopeName;
    }

    public void setScopedProxyMode(ScopedProxyMode scopedProxyMode) {
        if (scopedProxyMode == null) {
            throw new IllegalArgumentException("'scopedProxyMode' must not be null");
        }
        this.scopedProxyMode = scopedProxyMode;
    }

    public ScopedProxyMode getScopedProxyMode() {
        return this.scopedProxyMode;
    }

}
