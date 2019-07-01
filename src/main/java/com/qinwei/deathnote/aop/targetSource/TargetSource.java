package com.qinwei.deathnote.aop.targetSource;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public interface TargetSource extends TargetClassAware {

    /**
     * 是否每次调用 getTarget() 都返回同一个对象
     */
    boolean isStatic();

    Object getTarget() throws Exception;

    void releaseTarget(Object target) throws Exception;

}
