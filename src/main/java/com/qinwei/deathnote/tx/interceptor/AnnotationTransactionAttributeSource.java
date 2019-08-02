package com.qinwei.deathnote.tx.interceptor;

import com.qinwei.deathnote.tx.annotation.TransactionAnnotationParser;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * @author qinwei
 * @date 2019-07-31
 */
public class AnnotationTransactionAttributeSource extends AbstractFallbackTransactionAttributeSource {

    private final boolean publicMethodsOnly;

    private final TransactionAnnotationParser annotationParsers;

    /**
     * @Transaction 注解 只对 public 方法生效
     */
    public AnnotationTransactionAttributeSource() {
        this(true);
    }

    public AnnotationTransactionAttributeSource(boolean publicMethodsOnly) {
        this.publicMethodsOnly = publicMethodsOnly;
        this.annotationParsers = new TransactionAnnotationParser();
    }


    @Override
    protected TransactionAttribute findTransactionAttribute(Class<?> clazz) {
        return determineTransactionAttribute(clazz);
    }

    @Override
    protected TransactionAttribute findTransactionAttribute(Method method) {
        return determineTransactionAttribute(method);
    }

    protected TransactionAttribute determineTransactionAttribute(AnnotatedElement element) {
        return this.annotationParsers.parseTransactionAnnotation(element);
    }

    @Override
    protected boolean allowPublicMethodsOnly() {
        return this.publicMethodsOnly;
    }
}
