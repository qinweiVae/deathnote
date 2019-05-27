package com.qinwei.deathnote.beans;

import com.qinwei.deathnote.beans.bean.RootBeanDefinition;
import com.qinwei.deathnote.beans.factory.AutowireCapableBeanFactory;
import com.qinwei.deathnote.beans.postprocessor.BeanPostProcessor;
import com.qinwei.deathnote.beans.postprocessor.InstantiationAwareBeanPostProcessor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author qinwei
 * @date 2019-05-22
 */
@Slf4j
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {

    @Override
    public <T> T createBean(Class<T> beanClass) {
        RootBeanDefinition bd = new RootBeanDefinition(beanClass);
        bd.setScope(SCOPE_PROTOTYPE);
        return (T) createBean(beanClass.getName(), bd, null);
    }

    @Override
    protected Object createBean(String beanName, RootBeanDefinition bd, Object[] args) {
        Class<?> resolvedClass = resolveBeanClass(bd);
        RootBeanDefinition mbdToUse = bd;
        if (resolvedClass != null && !bd.hasBeanClass() && bd.getBeanClassName() != null) {
            mbdToUse = new RootBeanDefinition(bd);
            mbdToUse.setBeanClass(resolvedClass);
        }
        try {
            Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
            if (bean != null) {
                return bean;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("BeanPostProcessor before instantiation of bean failed", ex);
        }
        try {
            return doCreateBean(beanName, mbdToUse, args);
        } catch (Exception ex) {
            throw new IllegalStateException("Unexpected exception during bean creation", ex);
        }
    }

    protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final Object[] args) {
        return null;
    }

    protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition bd) {
        Object bean = null;
        if (hasInstantiationAwareBeanPostProcessors()) {
            Class<?> beanClass = resolveBeanClass(bd);
            if (beanClass != null) {
                bean = applyBeanPostProcessorsBeforeInstantiation(beanClass, beanName);
                if (bean != null) {
                    bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
                }
            }
        }
        return bean;
    }

    protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {
        return getBeanPostProcessors().stream()
                .filter(beanPostProcessor -> beanPostProcessor instanceof InstantiationAwareBeanPostProcessor)
                .map(beanPostProcessor -> (InstantiationAwareBeanPostProcessor) beanPostProcessor)
                .map(ibp -> ibp.postProcessBeforeInstantiation(beanClass, beanName))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

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

    @Override
    public void autowireBean(Object existingBean) {

    }

    @Override
    public Object createBean(Class<?> beanClass, int autowireMode) {
        return null;
    }

    @Override
    public Object initializeBean(Object existingBean, String beanName) {
        return null;
    }

    @Override
    public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) {
        Object result = existingBean;
        for (BeanPostProcessor processor : getBeanPostProcessors()) {
            Object current = processor.postProcessBeforeInitialization(result, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }

    @Override
    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) {
        Object result = existingBean;
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            Object current = beanPostProcessor.postProcessAfterInitialization(existingBean, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }
}
