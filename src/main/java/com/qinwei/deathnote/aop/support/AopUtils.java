package com.qinwei.deathnote.aop.support;

import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.aspectj.Pointcut;
import com.qinwei.deathnote.aop.aspectj.PointcutAdvisor;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.CollectionUtils;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public class AopUtils {

    public static final String ORIGINAL_TARGET_CLASS = "originalTargetClass";

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
}
