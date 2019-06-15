package com.qinwei.deathnote.beans.postprocessor;

import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;

/**
 * @author qinwei
 * @date 2019-06-13
 */
public interface BeanFactoryPostProcessor {

    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory);
}
