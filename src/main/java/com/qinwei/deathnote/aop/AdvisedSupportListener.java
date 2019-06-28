package com.qinwei.deathnote.aop;

import com.qinwei.deathnote.aop.support.AdvisedSupport;

/**
 * @author qinwei
 * @date 2019-06-28
 */
public interface AdvisedSupportListener {

    void activated(AdvisedSupport advised);

    void adviceChanged(AdvisedSupport advised);
}
