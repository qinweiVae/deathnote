package com.qinwei.deathnote.beans;

import com.qinwei.deathnote.beans.bean.BeanWrapper;
import com.qinwei.deathnote.beans.bean.BeanWrapperImpl;
import com.qinwei.deathnote.beans.bean.DisposableBeanAdapter;
import com.qinwei.deathnote.beans.bean.InitializingBean;
import com.qinwei.deathnote.beans.bean.RootBeanDefinition;
import com.qinwei.deathnote.beans.factory.AutowireCapableBeanFactory;
import com.qinwei.deathnote.beans.postprocessor.BeanPostProcessor;
import com.qinwei.deathnote.beans.postprocessor.InstantiationAwareBeanPostProcessor;
import com.qinwei.deathnote.beans.postprocessor.SmartInstantiationAwareBeanPostProcessor;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.CollectionUtils;
import com.qinwei.deathnote.utils.ObjectUtils;
import com.qinwei.deathnote.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author qinwei
 * @date 2019-05-22
 */
@Slf4j
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {

    /**
     * 根据类型创建bean
     */
    @Override
    public <T> T createBean(Class<T> beanClass) {
        RootBeanDefinition bd = new RootBeanDefinition(beanClass);
        bd.setScope(SCOPE_PROTOTYPE);
        return (T) createBean(beanClass.getName(), bd, null);
    }

    /**
     * 创建bean
     */
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

    /**
     * 创建 Bean 对象
     */
    protected Object doCreateBean(final String beanName, final RootBeanDefinition bd, final Object[] args) {
        //创建 BeanWrapper
        BeanWrapper instanceWrapper = createBeanInstance(beanName, bd, args);
        //已经实例化的 beans
        Object bean = instanceWrapper.getWrappedInstance();
        // beans 属性注入
        populateBean(beanName, bd, instanceWrapper);
        // 初始化bean
        bean = initializeBean(beanName, bean, bd);
        //注册 DisposableBean  (只有单例才能注册)
        registerDisposableBeanIfNecessary(beanName, bean, bd);
        return bean;
    }

    /**
     * beans 属性注入
     */
    protected void populateBean(String beanName, RootBeanDefinition bd, BeanWrapper bw) {
        boolean continueWithPropertyPopulation = true;
        //InstantiationAwareBeanPostProcessor 扩展 postProcessAfterInstantiation
        if (hasInstantiationAwareBeanPostProcessors()) {
            continueWithPropertyPopulation = applyBeanPostProcessorsAfterInstantiation(beanName, bw);
        }
        if (!continueWithPropertyPopulation) {
            return;
        }
        Map<String, Object> propertyValue = bd.hasPropertyValues() ? bd.getPropertyValues() : null;
        Map<String, Object> result = new HashMap<>(8);
        // 获取 RootBeanDefinition 的 注入方式
        int autowireMode = bd.getResolvedAutowireMode();
        // 按 name 注入
        if (autowireMode == AUTOWIRE_BY_NAME) {
            autowireByName(beanName, bw, result);
        }
        // 按 type 注入
        if (autowireMode == AUTOWIRE_BY_TYPE) {
            autowireByType(beanName, bw, result);
        }
        propertyValue = result;
        if (CollectionUtils.isNotEmpty(propertyValue)) {
            applyPropertyValues(beanName, bw, propertyValue);
        }
    }

    /**
     * 通过调用对象的 setter 方法进行属性设置
     */
    protected void applyPropertyValues(String beanName, BeanWrapper bw, Map<String, Object> propertyValue) {
        if (propertyValue.isEmpty()) {
            return;
        }
        try {
            bw.setPropertyValues(propertyValue);
        } catch (Exception e) {
            throw new RuntimeException("Unable to populate bean , bean name : " + beanName);
        }
    }

    /**
     * 按 name 注入
     */
    protected void autowireByName(String beanName, BeanWrapper bw, Map<String, Object> result) {
        String[] propertyNames = findPropertiesFromBeanWrapper(bw);
        for (String propertyName : propertyNames) {
            if (containsBean(propertyName)) {
                //获取或者创建bean
                Object bean = getBean(propertyName);
                result.put(propertyName, bean);
                //注册bean的依赖关系 beanName 依赖于 propertyName
                registerDependentBean(propertyName, beanName);
            }
        }
    }


    /**
     * 按 type 注入
     */
    protected void autowireByType(String beanName, BeanWrapper bw, Map<String, Object> result) {
        Set<String> autowiredBeanNames = new LinkedHashSet<>(4);
        String[] propertyNames = findPropertiesFromBeanWrapper(bw);
        for (String propertyName : propertyNames) {
            PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
            // 对 bean 的属性进行解析,可能包括：数组、Collection 、Map 类型
            Object autowiredArgument = resolveDependency(beanName, pd, autowiredBeanNames);
            if (autowiredArgument != null) {
                result.put(propertyName, autowiredArgument);
            }
            for (String name : autowiredBeanNames) {
                //注册bean的依赖关系 beanName 依赖于 name
                registerDependentBean(name, beanName);
            }
            autowiredBeanNames.clear();
        }
    }

    /**
     * 初始化bean
     */
    protected Object initializeBean(final String beanName, final Object bean, RootBeanDefinition bd) {
        Object wrappedBean = bean;
        if (bd == null) {
            // 执行 BeanPostProcessor 的 postProcessBeforeInitialization
            wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
        }
        try {
            //执行 init 方法
            invokeInitMethods(beanName, wrappedBean, bd);
        } catch (Exception e) {
            throw new RuntimeException("Unable to invoke init method , beanName : " + beanName, e);
        }
        if (bd == null) {
            // 执行 BeanPostProcessor 的 postProcessAfterInitialization
            wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
        }
        return wrappedBean;
    }

    /**
     * 执行 init 方法
     */
    protected void invokeInitMethods(String beanName, Object bean, RootBeanDefinition bd) {
        boolean isInitializingBean = bean instanceof InitializingBean;
        //如果是 InitializingBean 执行其 afterPropertiesSet()方法
        if (isInitializingBean && (bd == null || !bd.isExternallyInitMethod("afterPropertiesSet"))) {
            ((InitializingBean) bean).afterPropertiesSet();
        }
        if (bd != null) {
            String initMethodName = bd.getInitMethodName();
            if (StringUtils.isNotEmpty(initMethodName) &&
                    !(isInitializingBean && "afterPropertiesSet".equals(initMethodName)) &&
                    !bd.isExternallyInitMethod(initMethodName)) {
                Method method = ClassUtils.findMethod(bd.getBeanClass(), initMethodName);
                if (method != null) {
                    try {
                        ClassUtils.makeAccessible(method);
                        method.invoke(bean);
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to invoke init method , method name : " + initMethodName, e);
                    }
                }
            }
        }
    }

    /**
     * 只有单例才能注册 DisposableBean
     */
    protected void registerDisposableBeanIfNecessary(String beanName, Object bean, RootBeanDefinition bd) {
        if (bd.isSingleton() && requiresDestruction(bean, bd)) {
            registerDisposableBean(beanName, new DisposableBeanAdapter(bean, beanName, bd, getBeanPostProcessors()));
        }
    }

    /**
     * 判断bean是否需要执行 销毁  方法
     */
    protected boolean requiresDestruction(Object bean, RootBeanDefinition bd) {
        return DisposableBeanAdapter.hasDestroyMethod(bean, bd) ||
                (hasDestructionAwareBeanPostProcessors() && DisposableBeanAdapter.hasApplicableProcessors(bean, getBeanPostProcessors()));
    }


    /**
     * 查出 BeanWrapper 中的所有 PropertyDescriptor (必须有setter方法的,且type不是简单类型的)
     */
    private String[] findPropertiesFromBeanWrapper(BeanWrapper bw) {
        PropertyDescriptor[] pds = bw.getPropertyDescriptors();
        Set<String> set = Arrays.stream(pds).
                filter(pd -> !ClassUtils.isSimpleProperty(pd.getPropertyType())).
                filter(pd -> pd.getWriteMethod() != null)
                .map(FeatureDescriptor::getName)
                .collect(Collectors.toCollection(TreeSet::new));
        return StringUtils.toArray(set);
    }

    /**
     * 创建 BeanWrapper 对象
     */
    protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition bd, Object[] args) {
        //解析 beans ，将 beans 类名解析为 class 引用
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
                //进行类型转换
                argsToUse[i] = getConversion().convertIfNecessary(args[i], parameterTypes[i]);
            }
            //实例化
            beanWrapper.setBeanInstance(ClassUtils.instantiateClass(candidate, argsToUse));
            return beanWrapper;
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
        RootBeanDefinition bd = new RootBeanDefinition(existingBean.getClass());
        bd.setScope(SCOPE_PROTOTYPE);
        BeanWrapper bw = new BeanWrapperImpl(existingBean);
        populateBean(bd.getBeanClassName(), bd, bw);
    }

    @Override
    public Object createBean(Class<?> beanClass, int autowireMode) {
        RootBeanDefinition bd = new RootBeanDefinition(beanClass);
        bd.setAutowireMode(autowireMode);
        bd.setScope(SCOPE_PROTOTYPE);
        return createBean(beanClass.getName(), bd, null);
    }

    @Override
    public Object initializeBean(Object existingBean, String beanName) {
        return initializeBean(beanName, existingBean, null);
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

    protected abstract Object resolveDependency(String beanName, PropertyDescriptor pd, Set<String> autowiredBeanNames);
}
