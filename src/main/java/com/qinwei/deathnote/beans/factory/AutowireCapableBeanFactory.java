package com.qinwei.deathnote.beans.factory;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public interface AutowireCapableBeanFactory extends BeanFactory {

    int AUTOWIRE_NO = 0;

    int AUTOWIRE_BY_NAME = 1;

    int AUTOWIRE_BY_TYPE = 2;

    int AUTOWIRE_CONSTRUCTOR = 3;

    <T> T createBean(Class<T> beanClass);

    void autowireBean(Object existingBean);

    Object createBean(Class<?> beanClass, int autowireMode);

    Object initializeBean(Object existingBean, String beanName);

    Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName);

    Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName);
}
