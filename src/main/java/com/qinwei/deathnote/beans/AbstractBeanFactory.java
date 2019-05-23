package com.qinwei.deathnote.beans;

import com.qinwei.deathnote.beans.alias.DefaultSingletonBeanRegistry;
import com.qinwei.deathnote.beans.extension.BeanPostProcessor;
import com.qinwei.deathnote.beans.factory.ConfigurableBeanFactory;
import com.qinwei.deathnote.config.convert.Conversion;
import com.qinwei.deathnote.config.convert.DefaultConversion;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements ConfigurableBeanFactory {

    private final List<BeanPostProcessor> beanPostProcessors = new CopyOnWriteArrayList<>();

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        assert beanPostProcessor != null : "BeanPostProcessor must not be null";
        beanPostProcessors.remove(beanPostProcessor);
        beanPostProcessors.add(beanPostProcessor);
    }

    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
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
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name must not be null");
        }
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

    private Conversion getConversion() {
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
        return containsSingleton(beanName);
    }
}
