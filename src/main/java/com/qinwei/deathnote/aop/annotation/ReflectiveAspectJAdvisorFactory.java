package com.qinwei.deathnote.aop.annotation;

import com.qinwei.deathnote.aop.AspectInstanceFactory;
import com.qinwei.deathnote.aop.aspectj.Advice;
import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.aspectj.AspectJExpressionPointcut;
import com.qinwei.deathnote.aop.aspectj.advice.AbstractAspectJAdvice;
import com.qinwei.deathnote.aop.aspectj.advice.AspectJAfterAdvice;
import com.qinwei.deathnote.aop.aspectj.advice.AspectJAfterReturningAdvice;
import com.qinwei.deathnote.aop.aspectj.advice.AspectJAfterThrowingAdvice;
import com.qinwei.deathnote.aop.aspectj.advice.AspectJAroundAdvice;
import com.qinwei.deathnote.aop.aspectj.advice.AspectJMethodBeforeAdvice;
import com.qinwei.deathnote.aop.support.ConvertingComparator;
import com.qinwei.deathnote.aop.support.InstanceComparator;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.support.convert.Converter;
import com.qinwei.deathnote.utils.AnnotationUtils;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.StringUtils;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author qinwei
 * @date 2019-07-01
 */
public class ReflectiveAspectJAdvisorFactory extends AbstractAspectJAdvisorFactory {

    /**
     * Advice列表是按照 Around --> Before --> After --> AfterReturning --> AfterThrowing 的顺序进行排序,如果相同再根据Advice的方法名进行自然排序.
     */
    private static final Comparator<Method> METHOD_COMPARATOR;

    static {
        // 按照 Around --> Before --> After --> AfterReturning --> AfterThrowing 的顺序
        Comparator<Method> adviceKindComparator = new ConvertingComparator<>(
                new InstanceComparator<>(
                        Around.class, Before.class, After.class, AfterReturning.class, AfterThrowing.class),
                new Converter<Method, Annotation>() {
                    @Override
                    public Annotation convert(Method method) {
                        AspectJAnnotation<?> annotation = AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(method);
                        return (annotation != null ? annotation.getAnnotation() : null);
                    }
                });
        //再根据Advice的方法名顺序进行排序
        Comparator<Method> methodNameComparator = new ConvertingComparator<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        }, new Converter<Method, String>() {
            @Override
            public String convert(Method method) {
                return method.getName();
            }
        });
        METHOD_COMPARATOR = adviceKindComparator.thenComparing(methodNameComparator);
    }

    private final ConfigurableListableBeanFactory beanFactory;

    public ReflectiveAspectJAdvisorFactory(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public List<Advisor> getAdvisors(AspectInstanceFactory aspectInstanceFactory) {
        Class<?> aspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
        String aspectName = aspectInstanceFactory.getAspectMetadata().getAspectName();
        // 验证 class 是否支持
        validate(aspectClass);

        List<Advisor> advisors = new ArrayList<>();
        // getAdvisorMethods 用于返回不包含 @Pointcut 注解的方法
        for (Method method : getAdvisorMethods(aspectClass)) {
            // 为每个方法分别调用 getAdvisor 方法
            Advisor advisor = getAdvisor(method, aspectInstanceFactory, advisors.size(), aspectName);
            if (advisor != null) {
                advisors.add(advisor);
            }
        }
        return advisors;
    }

    /**
     * 获取所有不包含 @Pointcut 注解 的方法，并排序
     */
    private List<Method> getAdvisorMethods(Class<?> aspectClass) {
        //查找所有父类及接口的方法
        List<Method> methodList = ClassUtils.getAllDeclaredMethods(aspectClass);
        List<Method> methods = new ArrayList<>();
        for (Method method : methodList) {
            // 排除掉 包含 @Pointcut 注解 的方法
            if (AnnotationUtils.findAnnotation((AnnotatedElement) method, Pointcut.class) == null) {
                methods.add(method);
            }
        }
        //对方法排序
        methods.sort(METHOD_COMPARATOR);
        return methods;
    }

    @Override
    public Advisor getAdvisor(Method adviceMethod, AspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName) {
        //验证 class 是否支持
        validate(aspectInstanceFactory.getAspectMetadata().getAspectClass());
        // 获取切点实现类
        AspectJExpressionPointcut expressionPointcut = getPointcut(adviceMethod, aspectInstanceFactory.getAspectMetadata().getAspectClass());
        if (expressionPointcut == null) {
            return null;
        }
        // 创建 Advisor 实现类
        return new PointcutAdvisorImpl(expressionPointcut, adviceMethod, this, aspectInstanceFactory, declarationOrder, aspectName);
    }

    private AspectJExpressionPointcut getPointcut(Method adviceMethod, Class<?> aspectClass) {
        // 获取方法上的 AspectJ 相关注解，包括 @Before，@After 等
        AspectJAnnotation<?> aspectJAnnotation = AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(adviceMethod);
        if (aspectJAnnotation == null) {
            return null;
        }
        // 创建一个 AspectJExpressionPointcut 对象
        AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut(aspectClass);
        // 设置切点表达式
        ajexp.setExpression(aspectJAnnotation.getPointcutExpression());
        return ajexp;
    }

    @Override
    public Advice getAdvice(Method adviceMethod, AspectJExpressionPointcut expressionPointcut,
                            AspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName) {
        Class<?> aspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
        //验证 class 是否支持
        validate(aspectClass);
        AspectJAnnotation<?> aspectJAnnotation = AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(adviceMethod);
        if (aspectJAnnotation == null) {
            return null;
        }
        if (!isAspect(aspectClass)) {
            throw new IllegalStateException("Advice must be declared inside an aspect type: Offending method '" + adviceMethod + "' in class [" + aspectClass.getName() + "]");
        }
        AbstractAspectJAdvice advice;
        // 按照注解类型生成相应的 Advice 实现类
        switch (aspectJAnnotation.getAnnotationType()) {
            case AtPointcut:
                return null;
            case AtAround:
                advice = new AspectJAroundAdvice(adviceMethod, expressionPointcut, aspectInstanceFactory);
                break;
            case AtBefore:
                advice = new AspectJMethodBeforeAdvice(adviceMethod, expressionPointcut, aspectInstanceFactory);
                break;
            case AtAfter:
                advice = new AspectJAfterAdvice(adviceMethod, expressionPointcut, aspectInstanceFactory);
                break;
            case AtAfterReturning:
                advice = new AspectJAfterReturningAdvice(adviceMethod, expressionPointcut, aspectInstanceFactory);
                AfterReturning afterReturningAnnotation = (AfterReturning) aspectJAnnotation.getAnnotation();
                if (StringUtils.isNotEmpty(afterReturningAnnotation.returning())) {
                    advice.setReturningName(afterReturningAnnotation.returning());
                }
                break;
            case AtAfterThrowing:
                advice = new AspectJAfterThrowingAdvice(adviceMethod, expressionPointcut, aspectInstanceFactory);
                AfterThrowing afterThrowingAnnotation = (AfterThrowing) aspectJAnnotation.getAnnotation();
                if (StringUtils.isNotEmpty(afterThrowingAnnotation.throwing())) {
                    advice.setThrowingName(afterThrowingAnnotation.throwing());
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported advice type on method: " + adviceMethod);
        }
        advice.setAspectName(aspectName);
        advice.setDeclarationOrder(declarationOrder);
        advice.calculateArgumentBindings();
        return advice;
    }
}
