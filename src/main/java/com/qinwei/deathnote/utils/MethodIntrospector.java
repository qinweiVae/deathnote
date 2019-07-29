package com.qinwei.deathnote.utils;

import com.qinwei.deathnote.context.support.BridgeMethodResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-07-29
 */
public class MethodIntrospector {

    public static <T> Map<Method, T> selectMethods(Class<?> targetType, final MetadataLookup<T> metadataLookup) {
        final Map<Method, T> methodMap = new LinkedHashMap<>();

        Set<Class<?>> handlerTypes = new LinkedHashSet<>();
        Class<?> specificHandlerType = null;

        if (!Proxy.isProxyClass(targetType)) {
            specificHandlerType = ClassUtils.getUserClass(targetType);
            handlerTypes.add(specificHandlerType);
        }
        handlerTypes.addAll(ClassUtils.getAllInterfacesAsSet(targetType));

        for (Class<?> currentHandlerType : handlerTypes) {
            final Class<?> targetClass = specificHandlerType != null ? specificHandlerType : currentHandlerType;
            List<Method> methods = ClassUtils.getAllDeclaredMethods(targetClass);
            for (Method method : methods) {
                Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
                T result = metadataLookup.inspect(specificMethod);
                if (result != null) {
                    Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
                    if (bridgedMethod == specificMethod || metadataLookup.inspect(bridgedMethod) == null) {
                        methodMap.put(specificMethod, result);
                    }
                }
            }
        }

        return methodMap;
    }

    public static Method selectInvocableMethod(Method method, Class<?> targetType) {
        if (method.getDeclaringClass().isAssignableFrom(targetType)) {
            return method;
        }
        try {
            String methodName = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (Class<?> ifc : targetType.getInterfaces()) {
                try {
                    return ifc.getMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException ex) {
                }
            }
            return targetType.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(String.format(
                    "Need to invoke method '%s' declared on target class '%s', " +
                            "but not found in any interface(s) of the exposed proxy type. " +
                            "Either pull the method up to an interface or switch to CGLIB " +
                            "proxies by enforcing proxy-target-class mode in your configuration.",
                    method.getName(), method.getDeclaringClass().getSimpleName()));
        }
    }

    @FunctionalInterface
    public interface MetadataLookup<T> {

        T inspect(Method method);
    }
}
