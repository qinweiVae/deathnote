package com.qinwei.deathnote.beans.bean;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-05-24
 */
public interface BeanDefinition extends AttributeAccessor {

    Map<String, Object> getPropertyValues();

    boolean hasPropertyValues();

    void setBeanClassName(String beanClassName);

    String getBeanClassName();

    Class<?> getBeanClass();

    void setScope(String scope);

    String getScope();

    void setLazyInit(boolean lazyInit);

    boolean isLazyInit();

    void setDependsOn(String... dependsOn);

    String[] getDependsOn();

    void setPrimary(boolean primary);

    boolean isPrimary();

    void setInitMethodName(String initMethodName);

    String getInitMethodName();

    void setDestroyMethodName(String destroyMethodName);

    String getDestroyMethodName();

    boolean isSingleton();

    boolean isPrototype();

    boolean isAbstract();

    /**
     * 用于 @Bean 注解的处理
     */
    void setFactoryMethod(Method factoryMethod);

    /**
     * 用于 @Bean 注解的处理
     */
    Method getFactoryMethod();

    /**
     * 添加了@Configuration 注解的bean name，用于 @Bean 注解的处理
     */
    void setFactoryBeanName(String factoryBeanName);

    /**
     * 添加了@Configuration 注解的bean name，用于 @Bean 注解的处理
     */
    String getFactoryBeanName();
}
