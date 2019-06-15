package com.qinwei.deathnote.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinwei
 * @date 2019-06-14
 */
public class GenericTypeUtils {

    private static final Map<Class<?>, Type[]> TYPE_CACHE = new ConcurrentHashMap<>(64);

    /**
     * 获取 Class 上的泛型,只能用于普通类的 泛型
     * <p>
     * 对于collection、map之类的集合不要使用
     */
    public static Class findGenericType(Class<?> clazz, int index) {
        Type[] genericType = TYPE_CACHE.get(clazz);
        if (genericType == null) {
            synchronized (TYPE_CACHE) {
                if (TYPE_CACHE.containsKey(clazz)) {
                    genericType = TYPE_CACHE.get(clazz);
                } else {
                    genericType = findGenericType(clazz);
                    if (genericType != null) {
                        TYPE_CACHE.put(clazz, genericType);
                    }
                }
            }
        }
        if (genericType == null) {
            return null;
        }
        if (index >= genericType.length) {
            throw new ArrayIndexOutOfBoundsException("Array index out of range: " + index);
        }
        Type type = genericType[index];
        // 对于 嵌套情况下需要特殊处理下
        if (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getRawType();
        }
        if (type instanceof Class) {
            return (Class) type;
        }
        return null;
    }

    /**
     * 获取 Class 上的泛型,只能用于普通类的 泛型
     * <p>
     * 对于collection、map之类的集合不要使用，否则得到是 E,K,V 这样的东西，很蛋疼
     */
    public static Type[] findGenericType(Class<?> clazz) {
        Type[] genericType = null;
        //先从父类查找
        Type type = clazz.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            genericType = pt.getActualTypeArguments();
        }
        if (genericType != null) {
            return genericType;
        }
        //再从接口查找
        Type[] types = clazz.getGenericInterfaces();
        for (Type t : types) {
            if (t instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) t;
                genericType = pt.getActualTypeArguments();
                if (genericType != null) {
                    break;
                }
            }
        }
        return genericType;
    }

    /**
     * 用于获取collection、map等集合的泛型（对于这些集合只能通过Method、Field 的方式才能得到正确的泛型)）
     * <p>
     * readWriter 为true 获取setter方法返回参数的泛型，readWriter 为false 获取getter方法传入参数的泛型
     */
    public static Class findGenericType(PropertyDescriptor pd, boolean readWriter, int index) {
        Type[] genericType = findGenericType(pd, readWriter);
        if (genericType == null) {
            return null;
        }
        if (index >= genericType.length) {
            throw new ArrayIndexOutOfBoundsException("Array index out of range: " + index);
        }
        Type type = genericType[index];
        // 对于 Map<String, List<Domain>> 这种嵌套情况下获取value的泛型时得到是ParameterizedType java.util.List<com.qinwei.deathnote.beans.bean.Domain> 需要特殊处理下
        if (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getRawType();
        }
        if (type instanceof Class) {
            return (Class) type;
        }
        return null;
    }

    /**
     * readWriter 为true 获取setter方法返回参数的泛型，readWriter 为false 获取getter方法传入参数的泛型
     */
    public static Type[] findGenericType(PropertyDescriptor pd, boolean readWriter) {
        if (readWriter) {
            Type returnType = pd.getReadMethod().getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                Type[] genericType = ((ParameterizedType) returnType).getActualTypeArguments();
                if (genericType != null) {
                    return genericType;
                }
            }
        } else {
            Type[] parameterTypes = pd.getWriteMethod().getGenericParameterTypes();
            return Arrays.stream(parameterTypes)
                    .filter(type -> type instanceof ParameterizedType)
                    .map(type -> ((ParameterizedType) type).getActualTypeArguments())
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
