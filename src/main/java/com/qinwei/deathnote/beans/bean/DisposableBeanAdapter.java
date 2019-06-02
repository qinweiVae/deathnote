package com.qinwei.deathnote.beans.bean;

import com.qinwei.deathnote.beans.postprocessor.BeanPostProcessor;
import com.qinwei.deathnote.beans.postprocessor.DestructionAwareBeanPostProcessor;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author qinwei
 * @date 2019-05-23
 */
@Slf4j
public class DisposableBeanAdapter implements DisposableBean {

    public static final String CLOSE_METHOD_NAME = "close";

    public static final String SHUTDOWN_METHOD_NAME = "shutdown";

    private List<DestructionAwareBeanPostProcessor> beanPostProcessors;

    private final Object bean;

    private final String beanName;

    private String destroyMethodName;

    public DisposableBeanAdapter(Object bean, String beanName, BeanDefinition bd, List<BeanPostProcessor> postProcessors) {
        if (bd instanceof RootBeanDefinition) {
            throw new IllegalArgumentException("beanDefinition can not cast to RootBeanDefinition");
        }
        RootBeanDefinition beanDefinition = (RootBeanDefinition) bd;
        this.bean = bean;
        this.beanName = beanName;
        beanPostProcessors = filterPostProcessor(postProcessors, bean);
        String destroyMethodName = inferDestroyMethodIfNecessary(bean, beanDefinition);
        if (destroyMethodName != null && !beanDefinition.isExternallyDestroyMethod(destroyMethodName)) {
            this.destroyMethodName = destroyMethodName;
        }
    }

    private String inferDestroyMethodIfNecessary(Object bean, RootBeanDefinition bd) {
        String destroyMethodName = bd.getDestroyMethodName();
        if (destroyMethodName == null) {
            if (!(bean instanceof DisposableBean)) {
                try {
                    return bean.getClass().getMethod(CLOSE_METHOD_NAME).getName();
                } catch (NoSuchMethodException e) {
                    try {
                        return bean.getClass().getMethod(SHUTDOWN_METHOD_NAME).getName();
                    } catch (NoSuchMethodException e1) {
                    }
                }
            }
        }
        return destroyMethodName;
    }

    private List<DestructionAwareBeanPostProcessor> filterPostProcessor(List<BeanPostProcessor> postProcessors, Object bean) {
        return postProcessors.stream()
                .filter(postProcessor -> postProcessor instanceof DestructionAwareBeanPostProcessor)
                .map(postProcessor -> (DestructionAwareBeanPostProcessor) postProcessor)
                .filter(destruction -> destruction.requiresDestruction(bean))
                .collect(Collectors.toList());
    }


    @Override
    public void destroy() {
        if (CollectionUtils.isNotEmpty(this.beanPostProcessors)) {
            for (DestructionAwareBeanPostProcessor postProcessor : beanPostProcessors) {
                postProcessor.postProcessBeforeDestruction(this.bean, this.beanName);
            }
        }
        try {
            ((DisposableBean) this.bean).destroy();
        } catch (Exception e) {
            log.warn("invoke destroy() failure , DisposableBean : {}", beanName, e);
        }
        if (destroyMethodName != null) {
            try {
                Method destroyMethod = bean.getClass().getDeclaredMethod(destroyMethodName);
                if (destroyMethod.getParameterCount() > 0) {
                    throw new IllegalStateException("has many parameter - not supported as destroy method");
                }
                ClassUtils.makeAccessible(destroyMethod);
                destroyMethod.invoke(bean);
            } catch (Exception e) {
                log.error("Unable to invoke method : {}", destroyMethodName, e);
            }
        }
    }
}
