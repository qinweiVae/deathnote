package com.qinwei.deathnote.aop.autoproxy;

import com.qinwei.deathnote.aop.support.AopUtils;
import com.qinwei.deathnote.aop.support.SingletonTargetSource;
import com.qinwei.deathnote.aop.support.TargetSource;
import com.qinwei.deathnote.beans.factory.BeanFactory;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.beans.postprocessor.InstantiationAwareBeanPostProcessor;
import com.qinwei.deathnote.context.aware.BeanFactoryAware;
import com.qinwei.deathnote.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinwei
 * @date 2019-06-26
 */
@Slf4j
public abstract class AbstractAutoProxyCreator extends ProxyProcessorSupport implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {

    private ConfigurableListableBeanFactory beanFactory;

    private final Map<Class, Object> earlyProxyReferences = new ConcurrentHashMap<>(16);

    private final Set<String> targetSourcedBeans = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    private final Map<Class, Boolean> advisedBeans = new ConcurrentHashMap<>(256);

    private final Map<Class, Class<?>> proxyTypes = new ConcurrentHashMap<>(16);

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
            throw new IllegalArgumentException("AdvisorAutoProxyCreator requires a ConfigurableListableBeanFactory: " + beanFactory);
        }
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    protected ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean != null) {
            Class<?> cacheKey = bean.getClass();
            //如果缓存中不存在的话
            if (earlyProxyReferences.remove(cacheKey) != bean) {
                // 如果需要，为 bean 生成代理对象
                return wrapIfNecessary(bean, beanName, cacheKey);
            }
        }
        return bean;
    }

    /**
     * 1.若 bean 是 AOP 基础设施类型，则直接返回
     * 2.为 bean 查找合适的通知器
     * 3.如果通知器数组不为空，则为 bean 生成代理对象，并返回该对象
     * 4.若数组为空，则返回原始 bean
     */
    protected Object wrapIfNecessary(Object bean, String beanName, Class cacheKey) {
        //如果缓存中存在，直接返回
        if (StringUtils.isNotEmpty(beanName) && targetSourcedBeans.contains(beanName)) {
            return bean;
        }
        //如果 bean class 不是 advise，直接返回
        if (Boolean.FALSE.equals(advisedBeans.get(cacheKey))) {
            return bean;
        }
        if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
            this.advisedBeans.put(cacheKey, Boolean.FALSE);
            return bean;
        }
        // 为目标 bean 查找合适的通知器
        Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName);
        //若 specificInterceptors != null 则为 bean 生成代理对象，否则直接返回 bean
        if (specificInterceptors != null) {
            this.advisedBeans.put(cacheKey, Boolean.TRUE);
            // 创建代理
            Object proxy = createProxy(bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
            this.proxyTypes.put(cacheKey, proxy.getClass());
            //返回代理对象，此时 IOC 容器输入 bean，得到 proxy。此时 beanName 对应的 bean 是代理对象，而非原始的 bean
            return proxy;
        }
        this.advisedBeans.put(cacheKey, Boolean.FALSE);
        return bean;
    }

    /**
     * 筛选合适的通知器
     */
    protected Object createProxy(Class<?> beanClass, String beanName, Object[] specificInterceptors, TargetSource targetSource) {
        //将 实际的 bean class 设置到 beanDefinition 里面
        AopUtils.exposeTargetClass(beanFactory, beanName, beanClass);
        return null;
    }

    /**
     * 用于子类覆盖
     */
    protected boolean shouldSkip(Class<?> beanClass, String beanName) {
        return false;
    }

    protected abstract Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName);
}
