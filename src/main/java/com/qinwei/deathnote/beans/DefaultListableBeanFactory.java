package com.qinwei.deathnote.beans;

import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
        implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

    private volatile List<String> beanDefinitionNames = new ArrayList<>(256);

    @Override
    public void preInstantiateSingletons() {

    }

    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        return new String[0];
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons) {
        return new String[0];
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        return null;
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons) {
        return null;
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
        return null;
    }

    @Override
    public <T extends Annotation> T findAnnotationOnBean(String beanName, Class<T> annotationType) {
        return null;
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        return null;
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        assert !StringUtils.isEmpty(beanName) : "beanName must not be null";
        assert beanDefinition != null : "beanDefinition must not be null";
        BeanDefinition existingDefinition = beanDefinitionMap.get(beanName);
        if (existingDefinition != null) {
            throw new UnsupportedOperationException("Cannot register bean definition [" + beanDefinition + "] for bean '" + beanName +
                    "': There is already [" + existingDefinition + "] bound.");
        }
        beanDefinitionMap.put(beanName, beanDefinition);
        beanDefinitionNames.add(beanName);

    }

    @Override
    public void removeBeanDefinition(String beanName) {
        assert !StringUtils.isEmpty(beanName) : "beanName must not be null";
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new IllegalArgumentException("No bean named '" + beanName + "' available");
        }
        beanDefinitionNames.remove(beanName);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        assert !StringUtils.isEmpty(beanName) : "beanName must not be null";
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new IllegalArgumentException("No bean named '" + beanName + "' available");
        }
        return beanDefinition;
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        assert !StringUtils.isEmpty(beanName) : "beanName must not be null";
        return beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return StringUtils.toArray(beanDefinitionNames);
    }

    @Override
    public int getBeanDefinitionCount() {
        return beanDefinitionMap.size();
    }
}
