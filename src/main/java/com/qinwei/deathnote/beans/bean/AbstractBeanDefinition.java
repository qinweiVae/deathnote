package com.qinwei.deathnote.beans.bean;

import static com.qinwei.deathnote.beans.factory.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static com.qinwei.deathnote.beans.factory.ConfigurableBeanFactory.SCOPE_SINGLETON;

/**
 * @author qinwei
 * @date 2019-05-24
 */
public abstract class AbstractBeanDefinition implements BeanDefinition {

    private volatile Object beanClass;

    private String scope = "";

    private boolean abstractFlag = false;

    private Boolean lazyInit;

    private String[] dependsOn;

    private boolean primary = false;

    private String initMethodName;

    private String destroyMethodName;

    @Override
    public void setBeanClassName(String beanClassName) {
        this.beanClass = beanClassName;
    }

    @Override
    public String getBeanClassName() {
        Object beanClassObject = this.beanClass;
        return beanClassObject instanceof Class
                ? ((Class<?>) beanClassObject).getName()
                : (String) beanClassObject;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public Class<?> getBeanClass() throws IllegalStateException {
        Object beanClassObject = this.beanClass;
        assert beanClassObject != null : "No bean class specified on bean definition";
        assert beanClassObject instanceof Class : "Bean class name [" + beanClassObject + "] has not been resolved into an actual Class";
        return (Class<?>) beanClassObject;
    }

    @Override
    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    @Override
    public boolean isLazyInit() {
        return this.lazyInit != null && this.lazyInit.booleanValue();
    }

    @Override
    public void setDependsOn(String... dependsOn) {
        this.dependsOn = dependsOn;
    }

    @Override
    public String[] getDependsOn() {
        return dependsOn;
    }

    @Override
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    @Override
    public boolean isPrimary() {
        return primary;
    }

    @Override
    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    @Override
    public String getInitMethodName() {
        return initMethodName;
    }

    @Override
    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }

    @Override
    public String getDestroyMethodName() {
        return destroyMethodName;
    }

    @Override
    public boolean isSingleton() {
        return SCOPE_SINGLETON.equals(scope) || "".equals(scope);
    }

    @Override
    public boolean isPrototype() {
        return SCOPE_PROTOTYPE.equals(this.scope);
    }

    @Override
    public boolean isAbstract() {
        return abstractFlag;
    }

    public void setAbstract(boolean abstractFlag) {
        this.abstractFlag = abstractFlag;
    }

}
