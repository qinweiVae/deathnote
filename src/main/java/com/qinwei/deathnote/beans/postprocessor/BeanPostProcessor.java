package com.qinwei.deathnote.beans.postprocessor;

/**
 * @author qinwei
 * @date 2019-05-21
 */
public interface BeanPostProcessor {

    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
