package com.qinwei.deathnote.beans.bean;

import com.qinwei.deathnote.beans.extension.DestructionAwareBeanPostProcessor;

import java.util.List;

/**
 * @author qinwei
 * @date 2019-05-23
 */
public class DisposableBeanAdapter implements DisposableBean {

    private List<DestructionAwareBeanPostProcessor> beanPostProcessors;

    @Override
    public void destroy() {

    }
}
