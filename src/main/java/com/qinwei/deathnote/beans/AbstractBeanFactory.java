package com.qinwei.deathnote.beans;

import com.qinwei.deathnote.beans.bean.RootBeanDefinition;
import com.qinwei.deathnote.beans.factory.ConfigurableBeanFactory;
import com.qinwei.deathnote.beans.postprocessor.BeanPostProcessor;
import com.qinwei.deathnote.beans.postprocessor.DestructionAwareBeanPostProcessor;
import com.qinwei.deathnote.beans.postprocessor.InstantiationAwareBeanPostProcessor;
import com.qinwei.deathnote.beans.registry.DefaultSingletonBeanRegistry;
import com.qinwei.deathnote.config.convert.Conversion;
import com.qinwei.deathnote.config.convert.DefaultConversion;
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
    public void destroyBean(String beanName, Object beanInstance) {

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

    public ClassLoader getBeanClassLoader() {
        return ClassUtils.getDefaultClassLoader();
    }

    protected abstract Object createBean(String beanName, RootBeanDefinition mbd, Object[] args);
}
