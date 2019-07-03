package com.qinwei.deathnote.context.support;

import lombok.Getter;
import lombok.Setter;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinwei
 * @date 2019-06-20
 */
@Getter
@Setter
public class ResolveType {

    private static final Map<Class<?>, Type[]> TYPE_CACHE = new ConcurrentHashMap<>(64);

    private String name;

    private Class type;

    private Field field;

    private Method method;

    private Integer parameterIndex;

    private ResolveType(Class type) {
        this.type = type;
    }

    public static ResolveType forType(Class clazz) {
        return new ResolveType(clazz);
    }

    public static ResolveType forType(Class clazz, String name) {
        ResolveType resolveType = new ResolveType(clazz);
        resolveType.setName(name);
        return resolveType;
    }

    public static ResolveType forType(PropertyDescriptor pd) {
        ResolveType resolveType = new ResolveType(pd.getPropertyType());
        resolveType.setName(pd.getName());
        resolveType.setMethod(pd.getWriteMethod());
        return resolveType;
    }

    public static ResolveType forType(Field field) {
        ResolveType resolveType = new ResolveType(field.getType());
        resolveType.setName(field.getName());
        resolveType.setField(field);
        return resolveType;
    }

    public static ResolveType forType(Method method) {
        return forType(method, null);
    }

    public static ResolveType forType(Method method, Integer parameterIndex) {
        ResolveType resolveType = new ResolveType(method.getReturnType());
        resolveType.setName(method.getName());
        resolveType.setMethod(method);
        resolveType.setParameterIndex(parameterIndex);
        return resolveType;
    }

    /**
     * 获取 Class 上的泛型,只能用于普通类的 泛型
     * <p>
     * 对于collection、map之类的集合不要使用
     */
    public Class resolveClass(int index) {
        return resolveClass(index, -1);
    }

    /**
     * 获取 Class 上的泛型,用于普通类的 嵌套 泛型
     * <p>
     * 对于collection、map之类的集合不要使用
     */
    public Class resolveClass(int index, int nestedIndex) {
        Type[] genericType = resolveTypeFromCache();
        return resolveType(genericType, index, nestedIndex);
    }

    private Type[] resolveTypeFromCache() {
        Type[] genericType = TYPE_CACHE.get(type);
        if (genericType == null) {
            synchronized (TYPE_CACHE) {
                if (TYPE_CACHE.get(type) == null) {
                    genericType = resolveGeneric();
                    if (genericType != null) {
                        TYPE_CACHE.put(type, genericType);
                    }
                } else {
                    genericType = TYPE_CACHE.get(type);
                }
            }
        }
        return genericType;
    }

    /**
     * 获取 Class 上的泛型,只能用于普通类的 泛型
     * <p>
     * 对于collection、map之类的集合不要使用，否则得到是 E,K,V 这样的东西，很蛋疼
     */
    public Type[] resolveGeneric() {
        Type[] genericType = null;
        //先从父类查找
        Type type = this.type.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            genericType = pt.getActualTypeArguments();
        }
        if (genericType != null) {
            return genericType;
        }
        //再从接口查找
        Type[] types = this.type.getGenericInterfaces();
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
     */
    public Class resolveSpecialType(int index) {
        return resolveSpecialTypeNested(index, -1);
    }

    /**
     * 用于获取collection、map等集合的 嵌套 泛型（对于这些集合只能通过Method、Field 的方式才能得到正确的泛型)）
     */
    public Class resolveSpecialTypeNested(int index, int nestedIndex) {
        Type[] genericType = resolveSpecialType().get(this.parameterIndex != null ? this.parameterIndex : 0);
        return resolveType(genericType, index, nestedIndex);
    }

    private Class resolveType(Type[] genericType, int index, int nestedIndex) {
        if (genericType == null) {
            return null;
        }
        if (index >= genericType.length) {
            throw new ArrayIndexOutOfBoundsException("Array index out of range: " + index);
        }
        Type type = genericType[index];
        // 对于 Map<String, List<Domain>> 这种嵌套情况下获取value的泛型时得到是ParameterizedType java.util.List<com.qinwei.deathnote.beans.bean.Domain> 需要特殊处理下
        if (type instanceof ParameterizedType) {
            if (nestedIndex == -1) {
                type = ((ParameterizedType) type).getRawType();
            } else {
                Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
                type = actualTypeArguments[nestedIndex];
                if (type instanceof ParameterizedType) {
                    type = ((ParameterizedType) type).getRawType();
                }
            }
        }
        if (type instanceof Class) {
            return (Class) type;
        }
        return null;
    }

    /**
     * 获取Field的泛型
     * <p>
     * 获取Method 中传入参数的泛型
     */
    public Map<Integer, Type[]> resolveSpecialType() {

        Map<Integer, Type[]> result = new HashMap<>(8);

        if (getField() != null) {
            Type filedType = getField().getGenericType();
            if (filedType instanceof ParameterizedType) {
                Type[] arguments = ((ParameterizedType) filedType).getActualTypeArguments();
                if (arguments != null) {
                    result.put(0, arguments);
                    return result;
                }
            }
        }

        if (getMethod() != null) {
            Type[] parameterTypes = getMethod().getGenericParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Type parameterType = parameterTypes[i];
                Type[] actualTypeArguments = null;
                if (parameterType instanceof ParameterizedType) {
                    actualTypeArguments = ((ParameterizedType) parameterType).getActualTypeArguments();
                } else {
                    actualTypeArguments = new Type[]{parameterType};
                }
                result.put(i, actualTypeArguments);
            }
        }
        return result;
    }

}
