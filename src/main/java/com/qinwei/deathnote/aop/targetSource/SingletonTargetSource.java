package com.qinwei.deathnote.aop.targetSource;

import java.io.Serializable;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public class SingletonTargetSource implements TargetSource, Serializable {

    private static final long serialVersionUID = -4121497267591374747L;

    /**
     * 就是 bean
     */
    private final Object target;


    public SingletonTargetSource(Object target) {
        assert target != null : "Target object must not be null";
        this.target = target;
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    /**
     * 拿到 bean
     */
    @Override
    public Object getTarget() {
        return this.target;
    }

    @Override
    public void releaseTarget(Object target) {

    }

    /**
     * bean 的 class
     */
    @Override
    public Class<?> getTargetClass() {
        return this.target.getClass();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SingletonTargetSource)) {
            return false;
        }
        SingletonTargetSource otherTargetSource = (SingletonTargetSource) other;
        return this.target.equals(otherTargetSource.target);
    }
}
