package com.qinwei.deathnote.aop;

import com.qinwei.deathnote.aop.annotation.AspectMetadata;

/**
 * @author qinwei
 * @date 2019-07-02
 */
public interface AspectInstanceFactory {

    Object getAspectInstance();

    AspectMetadata getAspectMetadata();
}
