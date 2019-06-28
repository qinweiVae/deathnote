package com.qinwei.deathnote.aop;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author qinwei
 * @date 2019-06-28
 */
public class DefaultAdvisorChainFactory implements AdvisorChainFactory {

    @Override
    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Advised config, Method method, Class<?> targetClass) {

        return null;
    }
}
