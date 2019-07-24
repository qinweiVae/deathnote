package com.qinwei.deathnote.aop.support;

import com.qinwei.deathnote.aop.Advised;
import com.qinwei.deathnote.aop.annotation.AnnotationAwareAspectJAutoProxyCreator;
import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.aspectj.Pointcut;
import com.qinwei.deathnote.aop.aspectj.PointcutAdvisor;
import com.qinwei.deathnote.aop.targetSource.SingletonTargetSource;
import com.qinwei.deathnote.aop.targetSource.TargetSource;
import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.beans.bean.RootBeanDefinition;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.context.support.BridgeMethodResolver;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.CollectionUtils;
import com.qinwei.deathnote.utils.ObjectUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public class AopUtils {

    private static final String ORIGINAL_TARGET_CLASS = "originalTargetClass";

    public static final String PRESERVE_TARGET_CLASS_ATTRIBUTE = "preserveTargetClass";

    public static final String AUTO_PROXY_CREATOR_BEAN_NAME = "internalAutoProxyCreator";


    public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
        return registerOrEscalateApcAsRequired(AnnotationAwareAspectJAutoProxyCreator.class, registry);
    }

    /**
     * 注册 BeanDefinition
     */
    private static BeanDefinition registerOrEscalateApcAsRequired(Class<?> cls, BeanDefinitionRegistry registry) {
        if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
            return null;
        }
        RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
        registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, beanDefinition);
        return beanDefinition;
    }

    /**
     * 设置 proxyTargetClass 属性
     */
    public static void forceAutoProxyCreatorToUseClassProxying(BeanDefinitionRegistry registry) {
        if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
            BeanDefinition definition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
            definition.getPropertyValues().put("proxyTargetClass", Boolean.TRUE);
        }
    }

    /**
     * 设置 exposeProxy 属性
     */
    public static void forceAutoProxyCreatorToExposeProxy(BeanDefinitionRegistry registry) {
        if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
            BeanDefinition definition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
            definition.getPropertyValues().put("exposeProxy", Boolean.TRUE);
        }
    }

    /**
     * 判断是否是代理类
     */
    public static boolean isAopProxy(Object object) {
        return Proxy.isProxyClass(object.getClass()) || ClassUtils.isCglibProxyClass(object.getClass());
    }

    /**
     * 将 实际的 bean class 设置到 beanDefinition 里面
     */
    public static void exposeTargetClass(ConfigurableListableBeanFactory beanFactory, String beanName, Class<?> targetClass) {
        if (beanName != null && beanFactory.containsBeanDefinition(beanName)) {
            beanFactory.getBeanDefinition(beanName).setAttribute(ORIGINAL_TARGET_CLASS, targetClass);
        }
    }

    public static Object getSingletonTarget(Object candidate) {
        if (candidate instanceof Advised) {
            TargetSource targetSource = ((Advised) candidate).getTargetSource();
            if (targetSource instanceof SingletonTargetSource) {
                return ((SingletonTargetSource) targetSource).getTarget();
            }
        }
        return null;
    }

    /**
     * 判断是否需要进行cglib代理
     */
    public static boolean shouldProxyTargetClass(ConfigurableListableBeanFactory beanFactory, String beanName) {
        if (beanName != null && beanFactory.containsBeanDefinition(beanName)) {
            BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
            return Boolean.TRUE.equals(bd.getAttribute(PRESERVE_TARGET_CLASS_ATTRIBUTE));
        }
        return false;
    }


    /**
     * 选可应用在 beanClass 上的 Advisor，通过 ClassFilter 和 MethodMatcher对目标类和方法进行匹配
     */
    public static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> beanClass) {
        if (CollectionUtils.isEmpty(candidateAdvisors)) {
            return candidateAdvisors;
        }
        List<Advisor> eligibleAdvisors = new ArrayList<>();
        for (Advisor advisor : candidateAdvisors) {
            if (canApply(advisor, beanClass)) {
                eligibleAdvisors.add(advisor);
            }
        }
        return eligibleAdvisors;
    }

    public static boolean canApply(Advisor advisor, Class<?> targetClass) {
        if (advisor instanceof PointcutAdvisor) {
            PointcutAdvisor pointcutAdvisor = (PointcutAdvisor) advisor;
            return canApply(pointcutAdvisor.getPointcut(), targetClass);
        }
        return true;
    }

    public static boolean canApply(Pointcut pointcut, Class<?> targetClass) {
        // 使用 ClassFilter 匹配 class
        if (!pointcut.getClassFilter().matches(targetClass)) {
            return false;
        }
        MethodMatcher methodMatcher = pointcut.getMethodMatcher();
        if (methodMatcher == MethodMatcher.TRUE) {
            return true;
        }
        Set<Class<?>> classes = new LinkedHashSet<>();
        //如果不是jdk 代理，那就是CGLib 代理，拿到它的父类（也就是被代理的class）
        if (!Proxy.isProxyClass(targetClass)) {
            classes.add(ClassUtils.getUserClass(targetClass));
        }
        /*
         * 查找当前类及其父类（以及父类的父类等等）所实现的接口，由于接口中的方法是 public，
         * 所以当前类可以继承其父类，和父类的父类中所有的接口方法
         */
        classes.addAll(ClassUtils.getAllInterfacesAsSet(targetClass));
        return classes.stream()
                .map(ClassUtils::getAllDeclaredMethods)
                .flatMap(Collection::stream)
                .anyMatch(method -> methodMatcher.matches(method, targetClass));
    }

    /**
     * 给定一个方法(可能来自接口)或者当前AOP调用中使用的目标类，如果存在相应的目标方法，则查找相应的目标方法。
     * 例如，方法可能是IFoo.bar()，目标类可能是DefaultFoo。这种情况下，方法可能是DefaultFoo.bar()
     */
    public static Method getMostSpecificMethod(Method method, Class<?> targetClass) {
        // 如果是 CGLib 代理类
        Class<?> specificTargetClass = (targetClass != null ? ClassUtils.getUserClass(targetClass) : null);
        // 解析Method  得到子类覆盖的方法
        Method resolvedMethod = ClassUtils.getMostSpecificMethod(method, specificTargetClass);
        // 如果是桥接方法
        return BridgeMethodResolver.findBridgedMethod(resolvedMethod);
    }

    /**
     * 得到代理接口,总是会 添加 Advised 作为代理接口
     */
    public static Class<?>[] completeProxiedInterfaces(AdvisedSupport advised) {
        Class<?>[] specifiedInterfaces = advised.getProxiedInterfaces();
        // 如果没有代理接口，拿到 advised 的 targetClass
        if (specifiedInterfaces.length == 0) {
            Class<?> targetClass = advised.getTargetClass();
            if (targetClass != null) {
                //如果targetClass（targetClass 就是用 SingletonTargetSource 封装了的 bean）是接口
                if (targetClass.isInterface()) {
                    advised.setInterfaces(targetClass);
                }
                //如果targetClass 是 jdk 动态代理的 类
                else if (Proxy.isProxyClass(targetClass)) {
                    advised.setInterfaces(targetClass.getInterfaces());
                }
                specifiedInterfaces = advised.getProxiedInterfaces();
            }
        }
        boolean addAdvised = !advised.isInterfaceProxied(Advised.class);
        int nonUserIfcCount = 0;
        if (addAdvised) {
            nonUserIfcCount++;
        }
        Class<?>[] proxiedInterfaces = new Class<?>[specifiedInterfaces.length + nonUserIfcCount];
        System.arraycopy(specifiedInterfaces, 0, proxiedInterfaces, 0, specifiedInterfaces.length);
        int index = specifiedInterfaces.length;
        if (addAdvised) {
            proxiedInterfaces[index] = Advised.class;
            index++;
        }
        return proxiedInterfaces;
    }

    /**
     * 解析method 的可变参数
     */
    public static Object[] adaptArgumentsIfNecessary(Method method, Object[] arguments) {
        if (ObjectUtils.isEmpty(arguments)) {
            return new Object[0];
        }
        //判断方法是否包含可变参数
        if (method.isVarArgs()) {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == arguments.length) {
                // 可变参数只能在method参数的最后一个位置
                int varargIndex = paramTypes.length - 1;
                Class<?> varargType = paramTypes[varargIndex];
                if (varargType.isArray()) {
                    Object varargArray = arguments[varargIndex];
                    if (varargArray instanceof Object[] && !varargType.isInstance(varargArray)) {
                        Object[] newArguments = new Object[arguments.length];
                        System.arraycopy(arguments, 0, newArguments, 0, varargIndex);
                        Class<?> targetElementType = varargType.getComponentType();
                        int varargLength = Array.getLength(varargArray);
                        Object newVarargArray = Array.newInstance(targetElementType, varargLength);
                        System.arraycopy(varargArray, 0, newVarargArray, 0, varargLength);
                        newArguments[varargIndex] = newVarargArray;
                        return newArguments;
                    }
                }
            }
        }
        return arguments;
    }

    public static boolean equalsInProxy(AdvisedSupport a, AdvisedSupport b) {
        return a == b ||
                (equalsProxiedInterfaces(a, b) && equalsAdvisors(a, b) && a.getTargetSource().equals(b.getTargetSource()));
    }

    public static boolean equalsProxiedInterfaces(AdvisedSupport a, AdvisedSupport b) {
        return Arrays.equals(a.getProxiedInterfaces(), b.getProxiedInterfaces());
    }

    public static boolean equalsAdvisors(AdvisedSupport a, AdvisedSupport b) {
        return Arrays.equals(a.getAdvisors(), b.getAdvisors());
    }
}
