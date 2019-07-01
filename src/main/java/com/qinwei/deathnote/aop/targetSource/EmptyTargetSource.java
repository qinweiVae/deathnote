package com.qinwei.deathnote.aop.targetSource;

import java.io.Serializable;

/**
 * @author qinwei
 * @date 2019-06-28
 */
public class EmptyTargetSource implements TargetSource, Serializable {

    private static final long serialVersionUID = 2511851162302750931L;

    private final Class<?> targetClass;

    private final boolean isStatic;

    public static final EmptyTargetSource INSTANCE = new EmptyTargetSource(null, true);

    public static EmptyTargetSource forClass(Class<?> targetClass) {
        return forClass(targetClass, true);
    }

    public static EmptyTargetSource forClass(Class<?> targetClass, boolean isStatic) {
        return (targetClass == null && isStatic ? INSTANCE : new EmptyTargetSource(targetClass, isStatic));
    }

    private EmptyTargetSource(Class<?> targetClass, boolean isStatic) {
        this.targetClass = targetClass;
        this.isStatic = isStatic;
    }

    @Override
    public boolean isStatic() {
        return this.isStatic;
    }

    @Override
    public Object getTarget() throws Exception {
        return null;
    }

    @Override
    public void releaseTarget(Object target) throws Exception {

    }

    @Override
    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    private Object readResolve() {
        return (this.targetClass == null && this.isStatic ? INSTANCE : this);
    }
}
