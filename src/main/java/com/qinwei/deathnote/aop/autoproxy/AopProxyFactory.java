package com.qinwei.deathnote.aop.autoproxy;

import com.qinwei.deathnote.aop.support.AdvisedSupport;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public interface AopProxyFactory {

    AopProxy createAopProxy(AdvisedSupport config);
}
