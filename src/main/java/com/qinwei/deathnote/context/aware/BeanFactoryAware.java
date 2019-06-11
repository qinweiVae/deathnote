package com.qinwei.deathnote.context.aware;

import com.qinwei.deathnote.beans.factory.BeanFactory;

/**
 * @author qinwei
 * @date 2019-06-11
 */
public interface BeanFactoryAware extends Aware {

    void setBeanFactory(BeanFactory beanFactory);
}
