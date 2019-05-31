package com.qinwei.deathnote.beans.postprocessor;

import java.lang.reflect.Constructor;

/**
 * @author qinwei
 * @date 2019-05-28
 */
public interface SmartInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessor {

    default Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) {
        return null;
    }
}
