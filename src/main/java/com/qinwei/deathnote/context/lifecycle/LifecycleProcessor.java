package com.qinwei.deathnote.context.lifecycle;

/**
 * @author qinwei
 * @date 2019-06-11
 */
public interface LifecycleProcessor extends Lifecycle {

    void onRefresh();

    void onClose();
}
