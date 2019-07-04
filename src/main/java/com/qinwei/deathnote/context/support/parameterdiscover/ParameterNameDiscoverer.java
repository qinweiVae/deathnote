package com.qinwei.deathnote.context.support.parameterdiscover;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author qinwei
 * @date 2019-07-03
 */
public interface ParameterNameDiscoverer {

    String[] getParameterNames(Method method);

    String[] getParameterNames(Constructor<?> ctor);
}
