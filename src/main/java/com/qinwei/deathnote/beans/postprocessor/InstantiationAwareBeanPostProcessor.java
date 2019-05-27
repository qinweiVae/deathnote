package com.qinwei.deathnote.beans.postprocessor;

/**
 * @author qinwei
 * @date 2019-05-27
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

    default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
        return null;
    }


    default boolean postProcessAfterInstantiation(Object bean, String beanName) {
        return true;
    }
}
