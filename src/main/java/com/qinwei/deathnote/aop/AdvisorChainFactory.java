package com.qinwei.deathnote.aop;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author qinwei
 * @date 2019-06-28
 */
public interface AdvisorChainFactory {

    /**
     * 获取 MethodInterceptor 的 调用链
     */
    List<Object> getInterceptorsAndDynamicInterceptionAdvice(Advised config, Method method, Class<?> targetClass);
}
