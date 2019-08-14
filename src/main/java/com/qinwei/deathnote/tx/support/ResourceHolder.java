package com.qinwei.deathnote.tx.support;

/**
 * @author qinwei
 * @date 2019-08-14
 */
public interface ResourceHolder {

    void reset();

    void unbound();

    boolean isVoid();
}
