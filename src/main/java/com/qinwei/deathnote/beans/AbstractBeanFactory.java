package com.qinwei.deathnote.beans;

import com.qinwei.deathnote.beans.bean.RootBeanDefinition;
import com.qinwei.deathnote.beans.factory.ConfigurableBeanFactory;
import com.qinwei.deathnote.beans.postprocessor.BeanPostProcessor;
import com.qinwei.deathnote.beans.postprocessor.DestructionAwareBeanPostProcessor;
import com.qinwei.deathnote.beans.postprocessor.InstantiationAwareBeanPostProcessor;
import com.qinwei.deathnote.beans.registry.DefaultSingletonBeanRegistry;
import com.qinwei.deathnote.support.convert.Conversion;
import com.qinwei.deathnote.support.convert.DefaultConversion;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.StringUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements ConfigurableBeanFactory {

    private final List<BeanPostProcessor> beanPostProcessors = new CopyOnWriteArrayList<>();

    private volatile boolean hasInstantiationAwareBeanPostProcessors;

    private volatile boolean hasDestructionAwareBeanPostProcessors;

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        assert beanPostProcessor != null : "BeanPostProcessor must not be null";
        if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
            this.hasInstantiationAwareBeanPostProcessors = true;
        }
        if (beanPostProcessor instanceof DestructionAwareBeanPostProcessor) {
            this.hasDestructionAwareBeanPostProcessors = true;
        }
        beanPostProcessors.remove(beanPostProcessor);
        beanPostProcessors.add(beanPostProcessor);
    }

    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    protected boolean hasInstantiationAwareBeanPostProcessors() {
        return this.hasInstantiationAwareBeanPostProcessors;
    }

    protected boolean hasDestructionAwareBeanPostProcessors() {
        return this.hasDestructionAwareBeanPostProcessors;
    }

    @Override
    public Object getBean(String name) {
        return getBean(name, null);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) {
        assert !StringUtils.isEmpty(name) : "name must not be null";
        String beanName = transformedBeanName(name);
        Object bean = getSingleton(beanName);
        if (bean == null) {

            return null;
        }
        if (requiredType != null && !requiredType.isInstance(bean)) {
            return getConversion().convert(bean, requiredType);
        }
        return (T) bean;
    }

    protected Conversion getConversion() {
        return DefaultConversion.getInstance();
    }

    private String transformedBeanName(String name) {
        return canonicalName(name);
    }

    @Override
    public String[] getAliases(String name) {
        String beanName = transformedBeanName(name);
        return super.getAliases(beanName);
    }

    @Override
    public boolean containsBean(String name) {
        String beanName = transformedBeanName(name);
        return super.containsSingleton(beanName);
    }

    /**
     * 解析 bean ，将 bean 类名解析为 class 引用
     */
    protected Class<?> resolveBeanClass(final RootBeanDefinition bd) {
        if (bd.hasBeanClass()) {
            return bd.getBeanClass();
        }
        ClassLoader beanClassLoader = getBeanClassLoader();
        String className = bd.getBeanClassName();
        try {
            return beanClassLoader.loadClass(className);
        } catch (ClassNotFoundException ignore) {

        }
        return bd.resolveBeanClass(beanClassLoader);
    }

    protected ClassLoader getBeanClassLoader() {
        return ClassUtils.getDefaultClassLoader();
    }

    @Override
    public boolean isTypeMatch(String beanName, Class<?> typeToMatch) {
        if (typeToMatch == null) {
            return true;
        }
        Object singleton = getSingleton(beanName);
        if (singleton != null) {
            return typeToMatch.isInstance(singleton) || ClassUtils.isAssignable(typeToMatch, singleton.getClass());
        }
        return false;
    }

    @Override
    public Class<?> getType(String name) {
        Object singleton = getSingleton(name);
        if (singleton != null) {
            return singleton.getClass();
        }
        return null;
    }

    protected abstract Object createBean(String beanName, RootBeanDefinition mbd, Object[] args);

    protected abstract boolean containsBeanDefinition(String beanName);
}
