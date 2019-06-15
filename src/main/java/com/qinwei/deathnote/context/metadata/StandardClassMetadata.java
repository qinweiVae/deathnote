package com.qinwei.deathnote.context.metadata;

import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * @author qinwei
 * @date 2019-06-14
 */
public class StandardClassMetadata implements ClassMetadata {

    private final Class<?> introspectedClass;

    public StandardClassMetadata(Class<?> introspectedClass) {
        assert introspectedClass != null : "Class must not be null";
        this.introspectedClass = introspectedClass;
    }

    public final Class<?> getIntrospectedClass() {
        return this.introspectedClass;
    }

    @Override
    public String getClassName() {
        return this.introspectedClass.getName();
    }

    @Override
    public boolean isInterface() {
        return this.introspectedClass.isInterface();
    }

    @Override
    public boolean isAnnotation() {
        return this.introspectedClass.isAnnotation();
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(this.introspectedClass.getModifiers());
    }

    @Override
    public boolean isConcrete() {
        return !(isInterface() || isAbstract());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(this.introspectedClass.getModifiers());
    }

    @Override
    public boolean hasEnclosingClass() {
        return (this.introspectedClass.getEnclosingClass() != null);
    }

    @Override
    public String getEnclosingClassName() {
        Class<?> enclosingClass = this.introspectedClass.getEnclosingClass();
        return (enclosingClass != null ? enclosingClass.getName() : null);
    }

    @Override
    public boolean hasSuperClass() {
        return (this.introspectedClass.getSuperclass() != null);
    }

    @Override
    public String getSuperClassName() {
        Class<?> superClass = this.introspectedClass.getSuperclass();
        return (superClass != null ? superClass.getName() : null);
    }

    @Override
    public String[] getInterfaceNames() {
        return Arrays.stream(this.introspectedClass.getInterfaces())
                .map(Class::getName)
                .toArray(String[]::new);
    }

    @Override
    public String[] getMemberClassNames() {
        return Arrays.stream(this.introspectedClass.getDeclaredClasses())
                .map(Class::getName)
                .toArray(String[]::new);
    }
}
