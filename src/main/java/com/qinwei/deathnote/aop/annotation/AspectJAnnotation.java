package com.qinwei.deathnote.aop.annotation;

import com.qinwei.deathnote.utils.AnnotationUtils;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-07-02
 */
public class AspectJAnnotation<A extends Annotation> {

    private static Map<Class<?>, AspectJAnnotationType> annotationTypeMap = new HashMap<>(8);

    static {
        annotationTypeMap.put(Pointcut.class, AspectJAnnotationType.AtPointcut);
        annotationTypeMap.put(Around.class, AspectJAnnotationType.AtAround);
        annotationTypeMap.put(Before.class, AspectJAnnotationType.AtBefore);
        annotationTypeMap.put(After.class, AspectJAnnotationType.AtAfter);
        annotationTypeMap.put(AfterReturning.class, AspectJAnnotationType.AtAfterReturning);
        annotationTypeMap.put(AfterThrowing.class, AspectJAnnotationType.AtAfterThrowing);
    }

    private final A annotation;

    private final AspectJAnnotationType annotationType;

    private final String pointcutExpression;

    public AspectJAnnotation(A annotation) {
        this.annotation = annotation;
        this.annotationType = determineAnnotationType(annotation);
        try {
            this.pointcutExpression = resolveExpression(annotation);
        } catch (Exception ex) {
            throw new IllegalArgumentException(annotation + " is not a valid AspectJ annotation", ex);
        }
    }

    private AspectJAnnotationType determineAnnotationType(A annotation) {
        AspectJAnnotationType type = annotationTypeMap.get(annotation.annotationType());
        if (type != null) {
            return type;
        }
        throw new IllegalStateException("Unknown annotation type: " + annotation);
    }

    private String resolveExpression(A annotation) {
        //这里只用注解里面 value 的值，不支持其他的值
        Object val = AnnotationUtils.getValue(annotation, "value");
        if (val instanceof String) {
            String str = (String) val;
            if (!str.isEmpty()) {
                return str;
            }
        }
        throw new IllegalStateException("Failed to resolve expression: " + annotation);
    }

    public AspectJAnnotationType getAnnotationType() {
        return this.annotationType;
    }

    public A getAnnotation() {
        return this.annotation;
    }

    public String getPointcutExpression() {
        return this.pointcutExpression;
    }

    @Override
    public String toString() {
        return this.annotation.toString();
    }

    protected enum AspectJAnnotationType {
        AtPointcut, AtAround, AtBefore, AtAfter, AtAfterReturning, AtAfterThrowing
    }

}
