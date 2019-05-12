package com.qinwei.deathnote.utils;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-05-09
 */
public class ClassUtils {

    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap<>(8);

    private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new IdentityHashMap<>(8);

    static {
        primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
        primitiveWrapperTypeMap.put(Byte.class, byte.class);
        primitiveWrapperTypeMap.put(Character.class, char.class);
        primitiveWrapperTypeMap.put(Double.class, double.class);
        primitiveWrapperTypeMap.put(Float.class, float.class);
        primitiveWrapperTypeMap.put(Integer.class, int.class);
        primitiveWrapperTypeMap.put(Long.class, long.class);
        primitiveWrapperTypeMap.put(Short.class, short.class);

        primitiveWrapperTypeMap.entrySet().forEach(entry -> primitiveTypeToWrapperMap.put(entry.getValue(), entry.getKey()));

    }

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
        }
        if (cl == null) {
            cl = ClassUtils.class.getClassLoader();
            if (cl == null) {
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                }
            }
        }
        return cl;
    }

    public static boolean isAssignable(Class<?> lType, Class<?> rType) {
        if (lType.isAssignableFrom(rType)) {
            return true;
        }
        if (lType.isPrimitive()) {
            Class<?> resolvedPrimitive = primitiveWrapperTypeMap.get(rType);
            if (lType == resolvedPrimitive) {
                return true;
            }
        } else {
            Class<?> resolvedWrapper = primitiveTypeToWrapperMap.get(rType);
            if (resolvedWrapper != null && lType.isAssignableFrom(resolvedWrapper)) {
                return true;
            }
        }
        return false;
    }

}
