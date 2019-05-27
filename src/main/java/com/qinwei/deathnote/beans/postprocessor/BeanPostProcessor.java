package com.qinwei.deathnote.beans.postprocessor;

/**
 * @author qinwei
 * @date 2019-05-21
 */
public interface BeanPostProcessor {

    Object postProcessBeforeInitialization(Object bean, String beanName);

    Object postProcessAfterInitialization(Object bean, String beanName);
}
