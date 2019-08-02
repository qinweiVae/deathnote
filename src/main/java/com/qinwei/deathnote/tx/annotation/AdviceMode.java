package com.qinwei.deathnote.tx.annotation;

/**
 * @author qinwei
 * @date 2019-07-30
 */
public enum AdviceMode {

    /**
     * JDK proxy-based advice.
     */
    PROXY,

    /**
     * AspectJ weaving-based advice.
     */
    ASPECTJ
}
