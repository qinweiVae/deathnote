package com.qinwei.deathnote.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-05-09
 */
public class ClassUtils {

    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap<>(8);

    private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new IdentityHashMap<>(8);

    private static final Map<Class<?>, Object> DEFAULT_TYPE_VALUES;

    private static final Comparator<Executable> COMPARATOR = (e1, e2) -> {
        int result = Boolean.compare(Modifier.isPublic(e2.getModifiers()), Modifier.isPublic(e1.getModifiers()));
        return result != 0 ? result : Integer.compare(e2.getParameterCount(), e1.getParameterCount());
    };

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

        Map<Class<?>, Object> values = new HashMap<>();
        values.put(boolean.class, false);
        values.put(byte.class, (byte) 0);
        values.put(short.class, (short) 0);
        values.put(int.class, 0);
        values.put(long.class, (long) 0);
        DEFAULT_TYPE_VALUES = Collections.unmodifiableMap(values);
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

    public static <T> T instantiateClass(Class<T> clazz) {
        assert clazz != null : "class can not be null";
        if (clazz.isInterface()) {
            throw new IllegalArgumentException(clazz.getName() + " is an interface");
        }
        try {
            return instantiateClass(clazz.getDeclaredConstructor());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No default constructor found , class : " + clazz.getName());
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate class ", e);
        }
    }

    public static <T> T instantiateClass(Constructor<T> constructor, Object... args) {
        assert constructor != null : "constructor can not be null";
        try {
            makeAccessible(constructor);
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (args.length > parameterTypes.length) {
                throw new IllegalArgumentException("Can't specify more arguments than constructor parameters");
            }
            Object[] paramArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) {
                    Class<?> type = parameterTypes[i];
                    paramArgs[i] = type.isPrimitive() ? DEFAULT_TYPE_VALUES.get(type) : null;
                } else {
                    paramArgs[i] = args[i];
                }
            }
            return constructor.newInstance(paramArgs);
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate constructor {" + constructor + "}  with args {" + Arrays.toString(args) + "}");
        }
    }

    public static Class<?> forName(String className, ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = getDefaultClassLoader();
        }
        try {
            return Class.forName(className, false, classLoader);
        } catch (Exception e) {
            throw new IllegalStateException("Could not load class , className = {" + className + "}", e);
        }
    }

    public static void makeAccessible(Constructor<?> ctor) {
        if ((!Modifier.isPublic(ctor.getModifiers()) || !Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) && !ctor.isAccessible()) {
            ctor.setAccessible(true);
        }
    }

    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) ||
                !Modifier.isPublic(method.getDeclaringClass().getModifiers())) && !method.isAccessible()) {
            method.setAccessible(true);
        }
    }

    /**
     * 优先按public 排序，其次按照 构造器参数个数降序排
     */
    public static void sortConstructors(Constructor<?>[] constructors) {
        Arrays.sort(constructors, COMPARATOR);
    }

    /**
     * 查找class 上的注解,当前class没有，则从其所有的注解中寻找，再从其所有接口中寻找，再从其所有父类中寻找
     */
    public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
        A annotation = clazz.getDeclaredAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }
        for (Annotation ann : clazz.getDeclaredAnnotations()) {
            Class<? extends Annotation> type = ann.annotationType();
            annotation = findAnnotation(type, annotationType);
            if (annotation != null) {
                return annotation;
            }
        }
        for (Class<?> clazzInterface : clazz.getInterfaces()) {
            A a = findAnnotation(clazzInterface, annotationType);
            if (a != null) {
                return a;
            }
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass == null || superclass == Object.class) {
            return null;
        }
        return findAnnotation(superclass, annotationType);
    }

}
