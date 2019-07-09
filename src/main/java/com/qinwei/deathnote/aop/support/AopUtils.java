package com.qinwei.deathnote.aop.support;

import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.aspectj.Pointcut;
import com.qinwei.deathnote.aop.aspectj.PointcutAdvisor;
import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.context.support.BridgeMethodResolver;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.CollectionUtils;

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

    private static final String PRESERVE_TARGET_CLASS_ATTRIBUTE = "preserveTargetClass";

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
        for (Advisor advisor : eligibleAdvisors) {
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
