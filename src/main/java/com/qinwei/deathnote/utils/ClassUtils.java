package com.qinwei.deathnote.utils;

import java.io.Closeable;
import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author qinwei
 * @date 2019-05-09
 */
public class ClassUtils {

    public static final String ARRAY_SUFFIX = "[]";

    private static final String INTERNAL_ARRAY_PREFIX = "[";

    private static final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";

    private static final char PACKAGE_SEPARATOR = '.';

    private static final char PATH_SEPARATOR = '/';

    private static final char INNER_CLASS_SEPARATOR = '$';

    public static final String CGLIB_CLASS_SEPARATOR = "$$";

    public static final String CLASS_FILE_SUFFIX = ".class";

    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap<>(8);

    private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new IdentityHashMap<>(8);

    public static final Map<Class<?>, Object> DEFAULT_TYPE_VALUES;

    private static final Map<String, Class<?>> primitiveTypeNameMap = new HashMap<>(32);

    private static final Map<String, Class<?>> commonClassCache = new HashMap<>(64);

    /**
     * 优先按public 排序，其次按照 构造器参数个数降序排
     */
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
        values.put(double.class, (double) 0);
        DEFAULT_TYPE_VALUES = Collections.unmodifiableMap(values);

        Set<Class<?>> primitiveTypes = new HashSet<>(32);
        primitiveTypes.addAll(primitiveWrapperTypeMap.values());
        Collections.addAll(primitiveTypes, boolean[].class, byte[].class, char[].class,
                double[].class, float[].class, int[].class, long[].class, short[].class);
        primitiveTypes.add(void.class);
        for (Class<?> primitiveType : primitiveTypes) {
            primitiveTypeNameMap.put(primitiveType.getName(), primitiveType);
        }

        registerCommonClasses(Boolean[].class, Byte[].class, Character[].class, Double[].class,
                Float[].class, Integer[].class, Long[].class, Short[].class);
        registerCommonClasses(Number.class, Number[].class, String.class, String[].class,
                Class.class, Class[].class, Object.class, Object[].class);
        registerCommonClasses(Throwable.class, Exception.class, RuntimeException.class,
                Error.class, StackTraceElement.class, StackTraceElement[].class);
        registerCommonClasses(Enum.class, Iterable.class, Iterator.class, Enumeration.class,
                Collection.class, List.class, Set.class, Map.class, Map.Entry.class, Optional.class);

        Class<?>[] javaLanguageInterfaceArray = {Serializable.class, Externalizable.class,
                Closeable.class, AutoCloseable.class, Cloneable.class, Comparable.class};
        registerCommonClasses(javaLanguageInterfaceArray);
    }

    private static void registerCommonClasses(Class<?>... commonClasses) {
        for (Class<?> clazz : commonClasses) {
            commonClassCache.put(clazz.getName(), clazz);
        }
    }

    /**
     * 判断class 是否属于简单类型
     * 1. CharSequence 接口的实现类，比如 String
     * 2. Enum
     * 3. Date
     * 4. URI/URL
     * 5. Number 的继承类，比如 Integer/Long
     * 6. byte/short/int... 等基本类型
     * 7. Locale
     * 8. 以上所有类型的数组形式，比如 String[]、Date[]、int[] 等等
     */
    public static boolean isSimpleProperty(Class<?> clazz) {
        assert clazz != null : "Class must not be null";
        return isSimpleValueType(clazz) || (clazz.isArray() && isSimpleValueType(clazz.getComponentType()));
    }

    /**
     * 判断class 是否属于简单类型
     */
    public static boolean isSimpleValueType(Class<?> clazz) {
        return (clazz.isPrimitive() ||
                primitiveTypeToWrapperMap.containsKey(clazz) ||
                Enum.class.isAssignableFrom(clazz) ||
                CharSequence.class.isAssignableFrom(clazz) ||
                Number.class.isAssignableFrom(clazz) ||
                Date.class.isAssignableFrom(clazz) ||
                Temporal.class.isAssignableFrom(clazz) ||
                URI.class == clazz || URL.class == clazz ||
                Locale.class == clazz || Class.class == clazz);
    }

    /**
     * 判断class 是否是 内部类
     */
    public static boolean isInnerClass(Class<?> clazz) {
        return clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers());
    }

    /**
     * 获取默认的ClassLoader
     */
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

    /**
     * 判断subType是否可以转换为superType
     */
    public static boolean isAssignable(Class<?> superType, Class<?> subType) {
        if (superType.isAssignableFrom(subType)) {
            return true;
        }
        if (superType.isPrimitive()) {
            Class<?> resolvedPrimitive = primitiveWrapperTypeMap.get(subType);
            if (superType == resolvedPrimitive) {
                return true;
            }
        } else {
            Class<?> resolvedWrapper = primitiveTypeToWrapperMap.get(subType);
            if (resolvedWrapper != null && superType.isAssignableFrom(resolvedWrapper)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 使用默认无参构造器实例化class,并转换成 期望的类型
     */
    public static <T> T instantiateClass(Class<?> clazz, Class<T> expectedType) {
        if (!isAssignable(expectedType, clazz)) {
            throw new IllegalArgumentException("Unable to cast " + clazz + " to " + expectedType);
        }
        return (T) instantiateClass(clazz);
    }

    /**
     * 使用默认无参构造器实例化class
     */
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

    /**
     * 根据构造器和参数 实例化class
     */
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

    /**
     * 加载class
     */
    public static Class<?> forName(String className) {
        return forName(className, null);
    }

    /**
     * 加载class
     */
    public static Class<?> forName(String className, ClassLoader classLoader) {
        Class<?> clazz = resolvePrimitiveClassName(className);
        if (clazz == null) {
            clazz = commonClassCache.get(className);
        }
        if (clazz != null) {
            return clazz;
        }

        if (classLoader == null) {
            classLoader = getDefaultClassLoader();
        }

        // 对数组进行特殊处理

        // "java.lang.String[]" style arrays
        if (className.endsWith(ARRAY_SUFFIX)) {
            String elementClassName = className.substring(0, className.length() - ARRAY_SUFFIX.length());
            Class<?> elementClass = forName(elementClassName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[Ljava.lang.String;" style arrays
        if (className.startsWith(NON_PRIMITIVE_ARRAY_PREFIX) && className.endsWith(";")) {
            String elementName = className.substring(NON_PRIMITIVE_ARRAY_PREFIX.length(), className.length() - 1);
            Class<?> elementClass = forName(elementName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[[I" or "[[Ljava.lang.String;" style arrays
        if (className.startsWith(INTERNAL_ARRAY_PREFIX)) {
            String elementName = className.substring(INTERNAL_ARRAY_PREFIX.length());
            Class<?> elementClass = forName(elementName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        try {
            return Class.forName(className, false, classLoader);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load class , className = {" + className + "}", e);
        }
    }

    public static Class<?> resolvePrimitiveClassName(String name) {
        Class<?> result = null;
        if (name != null && name.length() <= 8) {
            result = primitiveTypeNameMap.get(name);
        }
        return result;
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

    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
                || Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    /**
     * 对constructor进行排序,优先按public 排序，其次按照 构造器参数个数降序排
     */
    public static void sortConstructors(Constructor<?>[] constructors) {
        Arrays.sort(constructors, COMPARATOR);
    }

    /**
     * 根据方法名和方法参数查找class对应的method,如果当前class没有找到,则找寻其父类,接口
     */
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() : findDeclaredMethods(searchType));
            for (Method method : methods) {
                // 如果 方法名 和 方法参数 完全一样
                if (methodName.equals(method.getName()) &&
                        (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    /**
     * 拿到所有方法，包括 clazz 接口 上的 default 方法
     */
    public static Method[] findDeclaredMethods(Class<?> clazz) {
        Method[] declaredMethods = clazz.getDeclaredMethods();
        List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
        // 如果没有 default 方法，直接返回
        if (CollectionUtils.isEmpty(defaultMethods)) {
            return declaredMethods;
        }
        Method[] result = new Method[declaredMethods.length + defaultMethods.size()];
        System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
        int index = declaredMethods.length;
        for (Method defaultMethod : defaultMethods) {
            result[index] = defaultMethod;
            index++;
        }
        return result;
    }

    /**
     * 拿到所有 接口 上的 default 方法
     */
    private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
        return Arrays.stream(clazz.getInterfaces())
                .flatMap(ifc -> Arrays.stream(ifc.getMethods()))
                .filter(ifcMethod -> !Modifier.isAbstract(ifcMethod.getModifiers()))
                .collect(Collectors.toList());
    }

    /**
     * 获取 class 的 类名
     */
    public static String getShortName(String className) {
        if (StringUtils.isEmpty(className)) {
            throw new IllegalArgumentException("Class name must not be empty");
        }
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        int nameEndIndex = className.indexOf(CGLIB_CLASS_SEPARATOR);
        if (nameEndIndex == -1) {
            nameEndIndex = className.length();
        }
        String shortName = className.substring(lastDotIndex + 1, nameEndIndex);
        shortName = shortName.replace(INNER_CLASS_SEPARATOR, PACKAGE_SEPARATOR);
        return shortName;
    }

    /**
     * 获取 class 所在的包路径
     */
    public static String getPackageName(Class<?> clazz) {
        return getPackageName(clazz.getName());
    }

    public static String getPackageName(String className) {
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        return (lastDotIndex != -1 ? className.substring(0, lastDotIndex) : "");
    }

    /**
     * 如果 class 是 CGLib 代理类 ，返回其父类
     */
    public static Class<?> getUserClass(Class<?> clazz) {
        if (isCglibProxyClass(clazz)) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass != Object.class) {
                return superclass;
            }
        }
        return clazz;
    }

    /**
     * 判断是否是 CGLib 代理类
     */
    public static boolean isCglibProxyClass(Class<?> clazz) {
        return clazz != null && clazz.getName().contains(CGLIB_CLASS_SEPARATOR);
    }

    /**
     * 查找当前类及其父类（以及父类的父类等等）所实现的接口
     */
    public static Set<Class<?>> getAllInterfacesAsSet(Class<?> clazz) {
        if (clazz.isInterface()) {
            return Collections.singleton(clazz);
        }
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            Class<?>[] ifs = current.getInterfaces();
            for (Class<?> inf : ifs) {
                interfaces.add(inf);
            }
            current = current.getSuperclass();
        }
        return interfaces;
    }

    /**
     * 查找所有父类及接口的方法
     */
    public static List<Method> getAllDeclaredMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        doGetAllDeclaredMethods(clazz, methods);
        return methods;
    }

    public static void doGetAllDeclaredMethods(Class<?> clazz, List<Method> result) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            result.add(method);
        }
        //拿到所有接口上的非抽象方法
        if (clazz.isInterface()) {
            for (Class<?> ifs : clazz.getInterfaces()) {
                for (Method method : methods) {
                    if (!Modifier.isAbstract(method.getModifiers())) {
                        result.add(method);
                    }
                }
            }
        }
        //递归获取父类的所有方法
        Class<?> superclass = clazz.getSuperclass();
        while (superclass != null && superclass != Object.class) {
            doGetAllDeclaredMethods(superclass, result);
        }
    }

    /**
     * 拿到 class 的 name (带.class的)
     */
    public static String getClassFileName(Class<?> clazz) {
        String className = clazz.getName();
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        return className.substring(lastDotIndex + 1) + CLASS_FILE_SUFFIX;
    }

    /**
     * 解析Method  得到子类覆盖的方法
     */
    public static Method getMostSpecificMethod(Method method, Class<?> targetClass) {
        if (targetClass != null && targetClass != method.getDeclaringClass() && isOverridable(method, targetClass)) {
            //如果是 Public 方法
            if (Modifier.isPublic(method.getModifiers())) {
                try {
                    return targetClass.getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException ex) {
                    return method;
                }
            }
            // 从父类 和 接口 中找
            else {
                Method specificMethod = findMethod(targetClass, method.getName(), method.getParameterTypes());
                return specificMethod != null ? specificMethod : method;
            }
        }
        return method;
    }

    /**
     * 判断 method 是否可用被 覆盖（如果method 是 public 或者 protected ，method 所在的定义类和 targetClass在 同一个包下面 ）
     */
    private static boolean isOverridable(Method method, Class<?> targetClass) {
        if (Modifier.isPrivate(method.getModifiers())) {
            return false;
        }
        if (Modifier.isPublic(method.getModifiers()) || Modifier.isProtected(method.getModifiers())) {
            return true;
        }
        return (targetClass == null ||
                getPackageName(method.getDeclaringClass()).equals(getPackageName(targetClass)));
    }


    /**
     * 创建jdk 代理
     */
    public static Class<?> createJdkProxy(Class<?>[] interfaces, ClassLoader classLoader) {
        if (interfaces == null) {
            throw new IllegalArgumentException("Interface array must not be empty");
        }
        return Proxy.getProxyClass(classLoader, interfaces);
    }

    public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
        return getMethodIfAvailable(clazz, methodName, paramTypes) != null;
    }

    public static Method getMethodIfAvailable(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class must not be null");
        }
        if (StringUtils.isEmpty(methodName)) {
            throw new IllegalArgumentException("Method name must not be null");
        }
        if (paramTypes != null) {
            try {
                return clazz.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ex) {
                return null;
            }
        } else {
            Set<Method> candidates = new HashSet<>(1);
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (methodName.equals(method.getName())) {
                    candidates.add(method);
                }
            }
            if (candidates.size() == 1) {
                return candidates.iterator().next();
            }
            return null;
        }
    }

    public static boolean isEqualsMethod(Method method) {
        if (method == null || !method.getName().equals("equals")) {
            return false;
        }
        Class<?>[] paramTypes = method.getParameterTypes();
        return (paramTypes.length == 1 && paramTypes[0] == Object.class);
    }

    public static boolean isHashCodeMethod(Method method) {
        return (method != null && method.getName().equals("hashCode") && method.getParameterCount() == 0);
    }

    public static boolean isToStringMethod(Method method) {
        return (method != null && method.getName().equals("toString") && method.getParameterCount() == 0);
    }

    public static boolean isFinalizeMethod(Method method) {
        return (method != null && method.getName().equals("finalize") && method.getParameterCount() == 0);
    }
}
