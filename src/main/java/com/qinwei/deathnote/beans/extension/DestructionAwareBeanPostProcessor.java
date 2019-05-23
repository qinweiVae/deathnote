package com.qinwei.deathnote.beans.extension;

import com.qinwei.deathnote.beans.factory.BeanFactory;

/**
 * @author qinwei
 * @date 2019-05-23
 */
public interface DestructionAwareBeanPostProcessor extends BeanFactory {

    void postProcessBeforeDestruction(Object bean, String beanName);

    default boolean requiresDestruction(Object bean) {
        return true;
    }
}
