package com.qinwei.deathnote.beans.postprocessor;

/**
 * @author qinwei
 * @date 2019-05-23
 */
public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {

    void postProcessBeforeDestruction(Object bean, String beanName);

    default boolean requiresDestruction(Object bean) {
        return true;
    }
}
