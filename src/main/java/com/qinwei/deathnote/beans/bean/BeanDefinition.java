package com.qinwei.deathnote.beans.bean;

import java.util.Map;

/**
 * @author qinwei
 * @date 2019-05-24
 */
public interface BeanDefinition {

    Map<String, Object> getPropertyValues();

    boolean hasPropertyValues();

    void setBeanClassName(String beanClassName);

    String getBeanClassName();

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
}
