package com.qinwei.deathnote.beans.bean;

import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.CollectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.qinwei.deathnote.beans.factory.AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;
import static com.qinwei.deathnote.beans.factory.AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;
import static com.qinwei.deathnote.beans.factory.AutowireCapableBeanFactory.AUTOWIRE_NO;
import static com.qinwei.deathnote.beans.factory.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static com.qinwei.deathnote.beans.factory.ConfigurableBeanFactory.SCOPE_SINGLETON;

/**
 * @author qinwei
 * @date 2019-05-24
 */
public abstract class AbstractBeanDefinition extends AttributeAccessorSupport implements BeanDefinition, Cloneable {

    private volatile Object beanClass;

    private String scope = "";

    private boolean abstractFlag = false;

    private Boolean lazyInit;

    private String[] dependsOn;

    private boolean primary = false;

    private int autowireMode = AUTOWIRE_NO;

    private String initMethodName;

    private String destroyMethodName;

    private Method factoryMethod;

    private String factoryBeanName;

    private Map<String, Object> propertyValues;

    @Override
    public Map<String, Object> getPropertyValues() {
        if (this.propertyValues == null) {
            this.propertyValues = new HashMap<>();
        }
        return this.propertyValues;
    }

    public void setPropertyValues(Map<String, Object> propertyValues) {
        this.propertyValues = propertyValues;
    }

    @Override
    public boolean hasPropertyValues() {
        return CollectionUtils.isNotEmpty(this.propertyValues);
    }

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

    @Override
    public Class<?> getBeanClass() throws IllegalStateException {
        Object beanClassObject = this.beanClass;
        assert beanClassObject != null : "No beans class specified on beans definition";
        assert beanClassObject instanceof Class : "Bean class name [" + beanClassObject + "] has not been resolved into an actual Class";
        return (Class<?>) beanClassObject;
    }

    public boolean hasBeanClass() {
        return this.beanClass instanceof Class;
    }

    /**
     * 解析beanClass,使用ClassLoader加载 class
     */
    public Class<?> resolveBeanClass(ClassLoader classLoader) {
        String className = getBeanClassName();
        if (className == null) {
            return null;
        }
        Class<?> resolvedClass = ClassUtils.forName(className, classLoader);
        this.beanClass = resolvedClass;
        return resolvedClass;
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

    public Boolean getLazyInit() {
        return this.lazyInit;
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

    public int getResolvedAutowireMode() {
        if (this.autowireMode == AUTOWIRE_NO) {
            Constructor<?>[] constructors = getBeanClass().getConstructors();
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() == 0) {
                    return AUTOWIRE_BY_TYPE;
                }
            }
            return AUTOWIRE_CONSTRUCTOR;
        }
        return this.autowireMode;
    }

    public void setAutowireMode(int autowireMode) {
        this.autowireMode = autowireMode;
    }

    public int getAutowireMode() {
        return this.autowireMode;
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
    public Method getFactoryMethod() {
        return factoryMethod;
    }

    @Override
    public void setFactoryMethod(Method factoryMethod) {
        this.factoryMethod = factoryMethod;
    }

    @Override
    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    @Override
    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
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

    @Override
    protected Object clone() {
        return cloneBeanDefinition();
    }

    protected abstract AbstractBeanDefinition cloneBeanDefinition();
}
