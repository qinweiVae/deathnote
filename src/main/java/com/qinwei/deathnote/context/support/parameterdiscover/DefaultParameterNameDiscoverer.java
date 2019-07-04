package com.qinwei.deathnote.context.support.parameterdiscover;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author qinwei
 * @date 2019-07-03
 */
public class DefaultParameterNameDiscoverer implements ParameterNameDiscoverer {

    private final List<ParameterNameDiscoverer> parameterNameDiscoverers = new LinkedList<>();

    public DefaultParameterNameDiscoverer() {
        //通过标准反射来获取 : java8可以通过反射获取参数名，但是需要使用-parameters参数开启这个功能
        addDiscoverer(new StandardReflectionParameterNameDiscoverer());
        //通过解析字节码文件的本地变量表来获取
        addDiscoverer(new LocalVariableTableParameterNameDiscoverer());
    }

    public void addDiscoverer(ParameterNameDiscoverer pnd) {
        this.parameterNameDiscoverers.add(pnd);
    }

    @Override
    public String[] getParameterNames(Method method) {
        return this.parameterNameDiscoverers.stream()
                .map(discoverer -> discoverer.getParameterNames(method))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String[] getParameterNames(Constructor<?> ctor) {
        return this.parameterNameDiscoverers.stream()
                .map(discoverer -> discoverer.getParameterNames(ctor))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
