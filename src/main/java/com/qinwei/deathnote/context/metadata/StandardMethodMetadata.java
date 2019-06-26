package com.qinwei.deathnote.context.metadata;

import com.qinwei.deathnote.utils.AnnotationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-06-14
 */
public class StandardMethodMetadata implements MethodMetadata {

    private final Method introspectedMethod;

    private final boolean nestedAnnotationsAsMap;


    public StandardMethodMetadata(Method introspectedMethod) {
        this(introspectedMethod, false);
    }

    public StandardMethodMetadata(Method introspectedMethod, boolean nestedAnnotationsAsMap) {
        assert introspectedMethod != null : "Method must not be null";
        this.introspectedMethod = introspectedMethod;
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
    }

    @Override
    public final Method getIntrospectedMethod() {
        return this.introspectedMethod;
    }

    @Override
    public String getMethodName() {
        return this.introspectedMethod.getName();
    }

    @Override
    public String getDeclaringClassName() {
        return this.introspectedMethod.getDeclaringClass().getName();
    }

    @Override
    public String getReturnTypeName() {
        return this.introspectedMethod.getReturnType().getName();
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(this.introspectedMethod.getModifiers());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(this.introspectedMethod.getModifiers());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(this.introspectedMethod.getModifiers());
    }

    @Override
    public boolean isOverridable() {
        return !isStatic() && !isFinal() && !Modifier.isPrivate(this.introspectedMethod.getModifiers());
    }

    @Override
    public boolean isAnnotated(String annotationName) {
        return AnnotationUtils.hasAnnotation(this.introspectedMethod, annotationName);
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName) {
        return AnnotationUtils.getAnnotationAttributes(this.introspectedMethod, annotationName, this.nestedAnnotationsAsMap);
    }

}
