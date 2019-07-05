package com.qinwei.deathnote.context.support;

import com.qinwei.deathnote.utils.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-07-03
 * <p>
 * java编译器采用bridge方法来兼容本该使用泛型的地方使用了非泛型的用法的问题
 * <p>
 * 当父类是泛型，而子类不是泛型时，如果子类重写了父类的方法，那么子类实际上会有两个同名的方法，参数为Object的方法则是桥接方法。
 */
public class BridgeMethodResolver {

    public static Method findBridgedMethod(Method bridgeMethod) {
        if (!bridgeMethod.isBridge()) {
            return bridgeMethod;
        }
        List<Method> candidateMethods = new ArrayList<>();
        List<Method> methods = ClassUtils.getAllDeclaredMethods(bridgeMethod.getDeclaringClass());
        for (Method method : methods) {
            if (isBridgedCandidateFor(method, bridgeMethod)) {
                candidateMethods.addAll(methods);
            }
        }
        // 如果刚好只找到一个匹配的
        if (candidateMethods.size() == 1) {
            return candidateMethods.get(0);
        }
        Method matchedMethod = searchCandidates(candidateMethods, bridgeMethod);
        if (matchedMethod != null) {
            return matchedMethod;
        }
        return bridgeMethod;
    }

    private static Method searchCandidates(List<Method> candidateMethods, Method bridgeMethod) {
        if (candidateMethods.isEmpty()) {
            return null;
        }
        for (Method candidateMethod : candidateMethods) {
            if (isBridgeMethodFor(bridgeMethod, candidateMethod, bridgeMethod.getDeclaringClass())) {
                return candidateMethod;
            }
        }
        return null;
    }

    public static boolean isBridgeMethodFor(Method bridgeMethod, Method candidateMethod, Class<?> declaringClass) {
        if (isResolvedTypeMatch(candidateMethod, bridgeMethod, declaringClass)) {
            return true;
        }
        Method method = findGenericDeclaration(bridgeMethod);
        return (method != null && isResolvedTypeMatch(method, candidateMethod, declaringClass));
    }

    private static Method findGenericDeclaration(Method bridgeMethod) {
        Class<?> superclass = bridgeMethod.getDeclaringClass().getSuperclass();
        // 递归获取父类的方法
        while (superclass != null && Object.class != superclass) {
            Method method = searchForMatch(superclass, bridgeMethod);
            if (method != null && !method.isBridge()) {
                return method;
            }
            superclass = superclass.getSuperclass();
        }
        // 拿到方法所在的class的所有接口
        Set<Class<?>> interfaces = ClassUtils.getAllInterfacesAsSet(bridgeMethod.getDeclaringClass());
        //从所有的接口中 寻找
        return searchInterfaces(interfaces.toArray(new Class[0]), bridgeMethod);
    }

    /**
     * 从所有的接口中 寻找
     */
    private static Method searchInterfaces(Class<?>[] interfaces, Method bridgeMethod) {
        for (Class<?> ifc : interfaces) {
            Method method = searchForMatch(ifc, bridgeMethod);
            if (method != null && !method.isBridge()) {
                return method;
            } else {
                method = searchInterfaces(ifc.getInterfaces(), bridgeMethod);
                if (method != null) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * 判断 泛型方法 与 桥接方法 的参数类型是否全部相同
     */
    private static boolean isResolvedTypeMatch(Method genericMethod, Method candidateMethod, Class<?> declaringClass) {
        // 拿到泛型方法的所有 泛型参数
        Type[] genericParameters = genericMethod.getGenericParameterTypes();
        // 拿到桥接方法的所有 参数
        Class<?>[] candidateParameters = candidateMethod.getParameterTypes();
        // 如果参数个数不同
        if (genericParameters.length != candidateParameters.length) {
            return false;
        }
        for (int i = 0; i < candidateParameters.length; i++) {
            Class<?> candidateParameter = candidateParameters[i];
            ResolveType resolveType = ResolveType.forType(genericMethod, i);
            if (!candidateParameter.equals(resolveType.resolveSpecialType(0))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 从指定的class 上查找 method
     */
    private static Method searchForMatch(Class<?> type, Method bridgeMethod) {
        try {
            return type.getDeclaredMethod(bridgeMethod.getName(), bridgeMethod.getParameterTypes());
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    /**
     * 判断candidateMethod 是否是 bridgeMethod桥接方法 的泛型方法
     * <p>
     * 如果 candidateMethod不是桥接方法，且不是bridgeMethod，并且方法名和方法参数的个数相同 则认为是
     */
    private static boolean isBridgedCandidateFor(Method candidateMethod, Method bridgeMethod) {
        return !candidateMethod.isBridge() && !candidateMethod.equals(bridgeMethod) &&
                candidateMethod.getName().equals(bridgeMethod.getName()) &&
                candidateMethod.getParameterCount() == bridgeMethod.getParameterCount();
    }

}
