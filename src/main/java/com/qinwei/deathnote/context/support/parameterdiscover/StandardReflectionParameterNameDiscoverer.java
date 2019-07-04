package com.qinwei.deathnote.context.support.parameterdiscover;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author qinwei
 * @date 2019-07-03
 */
public class StandardReflectionParameterNameDiscoverer implements ParameterNameDiscoverer {

    @Override
    public String[] getParameterNames(Method method) {
        return getParameterNames(method.getParameters());
    }

    @Override
    public String[] getParameterNames(Constructor<?> ctor) {
        return getParameterNames(ctor.getParameters());
    }

    private String[] getParameterNames(Parameter[] parameters) {
        String[] parameterNames = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            /*
             *  自动检测参数名是否存在（是否用-parameters选项编译了）
             *  如果jdk处于1.8版本，且编译时带上了-parameters 参数，那么获取的就是实际的参数名，
             *  如methodA(String username) 获取的就是username,否则获取的就是args0 后面的数字就是参数所在位置
             */
            if (!param.isNamePresent()) {
                return null;
            }
            parameterNames[i] = param.getName();
        }
        return parameterNames;
    }
}
