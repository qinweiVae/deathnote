package com.qinwei.deathnote.beans;

import com.qinwei.deathnote.beans.bean.BeanWrapper;
import com.qinwei.deathnote.beans.bean.BeanWrapperImpl;
import com.qinwei.deathnote.beans.bean.RootBeanDefinition;
import com.qinwei.deathnote.beans.factory.AutowireCapableBeanFactory;
import com.qinwei.deathnote.beans.postprocessor.BeanPostProcessor;
import com.qinwei.deathnote.beans.postprocessor.InstantiationAwareBeanPostProcessor;
import com.qinwei.deathnote.beans.postprocessor.SmartInstantiationAwareBeanPostProcessor;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
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
        RootBeanDefinition bdToUse = bd;
        if (resolvedClass != null && !bd.hasBeanClass() && bd.getBeanClassName() != null) {
            bdToUse = new RootBeanDefinition(bd);
            bdToUse.setBeanClass(resolvedClass);
        }
        // 给 BeanPostProcessors 一个机会用来返回一个代理类而不是真正的类实例
        // AOP 的功能就是基于这个地方
        Object bean = resolveBeforeInstantiation(beanName, bdToUse);
        if (bean != null) {
            return bean;
        }
        //创建 Bean 对象
        return doCreateBean(beanName, bdToUse, args);
    }

    protected Object doCreateBean(final String beanName, final RootBeanDefinition bd, final Object[] args) {
        //创建 BeanWrapper
        BeanWrapper instanceWrapper = createBeanInstance(beanName, bd, args);
        //已经实例化的 bean
        Object bean = instanceWrapper.getWrappedInstance();
        // bean 属性注入
        populateBean(beanName, bd, instanceWrapper);
        bean = initializeBean(beanName, bean, bd);

        return bean;
    }

    protected void populateBean(String beanName, RootBeanDefinition bd, BeanWrapper bw) {
        boolean continueWithPropertyPopulation = true;
        //InstantiationAwareBeanPostProcessor 扩展 postProcessAfterInstantiation
        if (hasInstantiationAwareBeanPostProcessors()) {
            continueWithPropertyPopulation = applyBeanPostProcessorsAfterInstantiation(beanName, bw);
        }
        if (!continueWithPropertyPopulation) {
            return;
        }
        int autowireMode = bd.getResolvedAutowireMode();
        if (autowireMode == AUTOWIRE_BY_NAME) {
            autowireByName(beanName, bd, bw);
        }
        if (autowireMode == AUTOWIRE_BY_TYPE) {
            autowireByType(beanName, bd, bw);
        }
    }

    protected void autowireByName(String beanName, RootBeanDefinition bd, BeanWrapper bw) {

    }

    protected void autowireByType(String beanName, RootBeanDefinition bd, BeanWrapper bw) {

    }

    protected Object initializeBean(final String beanName, final Object bean, RootBeanDefinition bd) {
        return null;
    }

    /**
     * 创建 BeanWrapper 对象
     */
    protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition bd, Object[] args) {
        //解析 bean ，将 bean 类名解析为 class 引用
        Class<?> beanClass = resolveBeanClass(bd);
        //SmartInstantiationAwareBeanPostProcessor 扩展
        Constructor<?>[] constructors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
        //从所有的构造器中选择合适的进行实例化
        if (constructors != null || bd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR || !ObjectUtils.isEmpty(args)) {
            return autowireConstructor(bd, constructors, args);
        }
        //使用默认无参构造器进行实例化
        return instantiateBean(bd);
    }

    /**
     * 从所有的构造器中选择合适的进行实例化
     */
    protected BeanWrapper autowireConstructor(RootBeanDefinition bd, Constructor<?>[] constructors, Object[] args) {
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl();
        Constructor<?>[] candidates = constructors;
        if (candidates == null) {
            candidates = bd.getBeanClass().getDeclaredConstructors();
        }
        //无参构造器
        if (candidates.length == 1) {
            Constructor<?> uniqueCandidate = candidates[0];
            if (uniqueCandidate.getParameterCount() == 0 && args == null) {
                beanWrapper.setBeanInstance(ClassUtils.instantiateClass(uniqueCandidate));
                return beanWrapper;
            }
        }
        //优先按public 排序，其次按照 构造器参数个数降序排
        ClassUtils.sortConstructors(candidates);
        Object[] argsToUse;
        //有参构造器
        for (Constructor<?> candidate : candidates) {
            //构造器的参数类型
            Class<?>[] parameterTypes = candidate.getParameterTypes();
            if (args != null && args.length > parameterTypes.length) {
                continue;
            }
            argsToUse = new Object[parameterTypes.length];

            for (int i = 0; i < parameterTypes.length; i++) {
                //如果传入了参数
                if (args != null) {
                    //如果可以强转
                    if (ClassUtils.isAssignable(parameterTypes[i], args[i].getClass())) {
                        argsToUse[i] = parameterTypes[i].cast(args[i]);
                    } else {
                        //如果不能强转，创建转换器进行类型转换
                        argsToUse[i] = getConversion().convert(args[i], parameterTypes[i]);
                    }
                } else {
                    //如果没有传入参数
                    argsToUse[i] = null;
                }
                //实例化
                beanWrapper.setBeanInstance(ClassUtils.instantiateClass(candidate, argsToUse));
                return beanWrapper;
            }
        }
        return beanWrapper;
    }

    /**
     * 使用默认无参构造器进行实例化
     */
    protected BeanWrapper instantiateBean(final RootBeanDefinition bd) {
        Object beanInstance = ClassUtils.instantiateClass(bd.getBeanClass());
        return new BeanWrapperImpl(beanInstance);
    }

    /**
     * SmartInstantiationAwareBeanPostProcessor 扩展 determineCandidateConstructors
     */
    protected Constructor<?>[] determineConstructorsFromBeanPostProcessors(Class<?> beanClass, String beanName) {
        if (beanClass != null && hasInstantiationAwareBeanPostProcessors()) {
            return getBeanPostProcessors().stream()
                    .filter(beanPostProcessor -> beanPostProcessor instanceof SmartInstantiationAwareBeanPostProcessor)
                    .map(beanPostProcessor -> (SmartInstantiationAwareBeanPostProcessor) beanPostProcessor)
                    .map(smart -> smart.determineCandidateConstructors(beanClass, beanName))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * 给 BeanPostProcessors 一个机会用来返回一个代理类而不是真正的类实例
     * AOP 的功能就是基于这个地方
     */
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

    /**
     * InstantiationAwareBeanPostProcessor 扩展 postProcessBeforeInstantiation
     */
    protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {
        return getBeanPostProcessors().stream()
                .filter(beanPostProcessor -> beanPostProcessor instanceof InstantiationAwareBeanPostProcessor)
                .map(beanPostProcessor -> (InstantiationAwareBeanPostProcessor) beanPostProcessor)
                .map(ibp -> ibp.postProcessBeforeInstantiation(beanClass, beanName))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * InstantiationAwareBeanPostProcessor 扩展 postProcessAfterInstantiation
     */
    private boolean applyBeanPostProcessorsAfterInstantiation(String beanName, BeanWrapper bw) {
        return getBeanPostProcessors().stream()
                .filter(beanPostProcessor -> beanPostProcessor instanceof InstantiationAwareBeanPostProcessor)
                .map(beanPostProcessor -> (InstantiationAwareBeanPostProcessor) beanPostProcessor)
                .allMatch(ibp -> ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName));
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

    /**
     * BeanPostProcessor 扩展 postProcessBeforeInitialization
     */
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

    /**
     * BeanPostProcessor 扩展 postProcessAfterInitialization
     */
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
