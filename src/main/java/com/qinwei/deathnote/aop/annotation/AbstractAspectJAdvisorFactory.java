package com.qinwei.deathnote.aop.annotation;

import com.qinwei.deathnote.utils.AnnotationUtils;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author qinwei
 * @date 2019-07-01
 */
public abstract class AbstractAspectJAdvisorFactory implements AspectJAdvisorFactory {

    private static final Class<?>[] ASPECTJ_ANNOTATION_CLASSES = new Class<?>[]{
            Pointcut.class, Around.class, Before.class, After.class, AfterReturning.class, AfterThrowing.class};

    @Override
    public boolean isAspect(Class<?> clazz) {
        return AnnotationUtils.findAnnotation((AnnotatedElement) clazz, Aspect.class) != null;
    }

    @Override
    public void validate(Class<?> aspectClass) {
        if (aspectClass.getSuperclass().getAnnotation(Aspect.class) != null &&
                !Modifier.isAbstract(aspectClass.getSuperclass().getModifiers())) {
            throw new IllegalStateException("[" + aspectClass.getName() + "] cannot extend concrete aspect [" +
                    aspectClass.getSuperclass().getName() + "]");
        }

        AjType<?> ajType = AjTypeSystem.getAjType(aspectClass);
        if (!ajType.isAspect()) {
            throw new IllegalStateException(aspectClass + " is not a aspect class");
        }
        PerClauseKind perClauseKind = ajType.getPerClause().getKind();
        if (perClauseKind != PerClauseKind.SINGLETON) {
            throw new IllegalStateException(aspectClass.getName() + " uses " + perClauseKind + " model: This is not supported in AOP.");
        }
    }

    /**
     * 获取 method 上第一个找到 的 aspectj 注解
     */
    protected static AspectJAnnotation<?> findAspectJAnnotationOnMethod(Method method) {
        for (Class<?> clazz : ASPECTJ_ANNOTATION_CLASSES) {
            AspectJAnnotation<?> foundAnnotation = findAnnotation(method, (Class<Annotation>) clazz);
            if (foundAnnotation != null) {
                return foundAnnotation;
            }
        }
        return null;
    }

    /**
     * 如果  method 上有 aspectj 注解，则封装成 AspectJAnnotation
     */
    private static <A extends Annotation> AspectJAnnotation<A> findAnnotation(Method method, Class<A> toLookFor) {
        A result = AnnotationUtils.findAnnotation(method, toLookFor);
        if (result != null) {
            return new AspectJAnnotation<>(result);
        } else {
            return null;
        }
    }

}
