package com.qinwei.deathnote.utils;

import com.qinwei.deathnote.context.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-14
 */
public class AnnotationUtils {

    private static final Set<Class<? extends Annotation>> primitiveAnnotations = new HashSet<>();

    static {
        //元注解
        primitiveAnnotations.add(Documented.class);
        primitiveAnnotations.add(Retention.class);
        primitiveAnnotations.add(Target.class);
        primitiveAnnotations.add(Inherited.class);
    }

    public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
        return findAnnotation(clazz, annotationType, true);
    }

    /**
     * 查找class 上的注解,当前class没有，则从其所有的注解中寻找，再从其所有接口中寻找，再从其所有父类中寻找
     * 如果 onlySelf 为true，则只在当前class及其所有注解中寻找
     */
    public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType, boolean onlySelf) {
        A annotation = findAnnotation((AnnotatedElement) clazz, annotationType);
        if (annotation != null) {
            return annotation;
        }
        if (onlySelf) {
            return null;
        }
        for (Class<?> clazzInterface : clazz.getInterfaces()) {
            A a = findAnnotation(clazzInterface, annotationType, false);
            if (a != null) {
                return a;
            }
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass == null || superclass == Object.class) {
            return null;
        }
        return findAnnotation(superclass, annotationType, false);
    }

    /**
     * 查找method 上的所有注解，当前method没有，则从其所有注解中查找，再从其父类的方法查找
     */
    public static <A extends Annotation> A findAnnotation(Method method, Class<A> annotationType) {
        A annotation = findAnnotation((AnnotatedElement) method, annotationType);
        if (annotation != null) {
            return annotation;
        }
        Class<?> clazz = method.getDeclaringClass();
        while (annotation == null) {
            clazz = clazz.getSuperclass();
            if (clazz == null || clazz == Object.class) {
                return null;
            }
            try {
                Method m = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
                annotation = findAnnotation((AnnotatedElement) m, annotationType);
            } catch (NoSuchMethodException e) {
                //ignore
            }
        }
        return annotation;
    }

    /**
     * Class、Method、Field、Constructor 均是 AnnotatedElement 的实现类
     */
    public static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
        A annotation = annotatedElement.getDeclaredAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }
        Annotation[] annotations = getDeclaredAnnotations(annotatedElement);
        for (Annotation anno : annotations) {
            Class<? extends Annotation> type = anno.annotationType();
            // 如果是 元注解 则返回，不然就是死循环了
            if (isPrimitiveAnnotation(type)) {
                continue;
            }
            annotation = findAnnotation((AnnotatedElement) type, annotationType);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * 判断 AnnotatedElement上 是否含有 annotationName
     */
    public static boolean hasAnnotation(AnnotatedElement element, String annotationName) {
        for (Annotation annotation : getDeclaredAnnotations(element)) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (!isPrimitiveAnnotation(type)) {
                if (type.getName().equals(annotationName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取 AnnotatedElement 上申明的所有注解
     */
    private static Annotation[] getDeclaredAnnotations(AnnotatedElement element) {
        return element.getDeclaredAnnotations();
    }


    /**
     * 判断 是否是 元注解
     */
    private static boolean isPrimitiveAnnotation(Class<? extends Annotation> type) {
        return primitiveAnnotations.contains(type);
    }

    /**
     * 获取AnnotatedElement 上找到的第一个注解的 AnnotationAttributes (就是一个map，注解的方法名 ---> 方法值)
     * 例如 @Order  (value,Integer.MAX_VALUE)
     */
    public static AnnotationAttributes getAnnotationAttributes(AnnotatedElement element, String annotationName, boolean nestedAnnotationsAsMap) {
        Annotation annotation = null;
        for (Annotation anno : getDeclaredAnnotations(element)) {
            Class<? extends Annotation> type = anno.annotationType();
            if (!isPrimitiveAnnotation(type) && type.getName().equals(annotationName)) {
                annotation = anno;
                break;
            }
        }
        if (annotation == null) {
            return null;
        }
        return getAnnotationAttributes(element, annotation, nestedAnnotationsAsMap);
    }

    /**
     * 获取AnnotatedElement 上注解的 AnnotationAttributes (就是一个map，注解的方法名 ---> 方法值)
     */
    public static AnnotationAttributes getAnnotationAttributes(AnnotatedElement element, Annotation annotation, boolean nestedAnnotationsAsMap) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        AnnotationAttributes attributes = new AnnotationAttributes(annotationType);
        for (Method method : getAttributeMethods(annotationType)) {
            try {
                //注解方法的值
                Object attributeValue = method.invoke(annotation);
                // 注解方法的默认值
                Object defaultValue = method.getDefaultValue();
                if (attributeValue == null) {
                    attributeValue = defaultValue;
                }
                attributes.put(method.getName(), adaptValue(element, attributeValue, nestedAnnotationsAsMap));
            } catch (Exception e) {
                throw new IllegalStateException("Could not obtain annotation attribute value for " + method, e);
            }
        }
        return attributes;
    }

    /**
     * 获取注解的 AnnotationAttributes
     */
    public static AnnotationAttributes getAnnotationAttributes(Annotation annotation) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        AnnotationAttributes attributes = new AnnotationAttributes(annotationType);
        for (Method method : getAttributeMethods(annotationType)) {
            try {
                //注解方法的值
                Object attributeValue = method.invoke(annotation);
                // 注解方法的默认值
                Object defaultValue = method.getDefaultValue();
                if (attributeValue == null) {
                    attributeValue = defaultValue;
                }
                attributes.put(method.getName(), attributeValue);
            } catch (Exception e) {
                throw new IllegalStateException("Could not obtain annotation attribute value for " + method, e);
            }
        }
        return attributes;
    }

    /**
     * 获取 注解上的所有 属性
     */
    public static List<Method> getAttributeMethods(Class<? extends Annotation> annotationType) {
        List<Method> methods = new ArrayList<>();
        for (Method method : annotationType.getDeclaredMethods()) {
            if (isAttributeMethod(method)) {
                ClassUtils.makeAccessible(method);
                methods.add(method);
            }
        }
        return methods;
    }

    /**
     * 判断是否是 注解中的方法
     */
    private static boolean isAttributeMethod(Method method) {
        return (method != null && method.getParameterCount() == 0 && method.getReturnType() != void.class);
    }

    /**
     * 如果value 还是 Annotation, nestedAnnotationsAsMap 为 true
     */
    private static Object adaptValue(AnnotatedElement annotatedElement, Object value, boolean nestedAnnotationsAsMap) {
        if (value instanceof Annotation) {
            Annotation annotation = (Annotation) value;
            if (nestedAnnotationsAsMap) {
                return getAnnotationAttributes(annotatedElement, annotation, true);
            }
        }
        if (value instanceof Annotation[]) {
            Annotation[] annotations = (Annotation[]) value;
            if (nestedAnnotationsAsMap) {
                AnnotationAttributes[] mappedAnnotations = new AnnotationAttributes[annotations.length];
                for (int i = 0; i < annotations.length; i++) {
                    mappedAnnotations[i] = getAnnotationAttributes(annotatedElement, annotations[i], true);
                }
                return mappedAnnotations;
            }
        }
        return value;
    }

    /**
     * 先找到 AnnotatedElement 上 指定 注解名称 的 Annotation，再拿到这个 Annotation 上的所有 注解名称
     */
    public static Set<String> getMetaAnnotationTypes(AnnotatedElement element, String annotationName) {
        return getMetaAnnotationTypes(getAnnotation(element, annotationName));
    }

    private static Set<String> getMetaAnnotationTypes(Annotation annotation) {
        if (annotation == null) {
            return Collections.emptySet();
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (Annotation anno : getDeclaredAnnotations(annotation.annotationType())) {
            if (!isPrimitiveAnnotation(anno.annotationType())) {
                result.add(anno.annotationType().getName());
            }
        }
        return result;
    }

    /**
     * 获取 AnnotatedElement 上 指定 注解名称 的 Annotation
     */
    public static Annotation getAnnotation(AnnotatedElement element, String annotationName) {
        for (Annotation annotation : getDeclaredAnnotations(element)) {
            if (!isPrimitiveAnnotation(annotation.annotationType())) {
                if (annotation.annotationType().getName().equals(annotationName)) {
                    return annotation;
                }
            }
        }
        return null;
    }

    /**
     * 获取注解上 指定 属性的值
     */
    public static Object getValue(Annotation annotation, String attributeName) {
        if (annotation == null || StringUtils.isEmpty(attributeName)) {
            return null;
        }
        try {
            Method method = annotation.annotationType().getDeclaredMethod(attributeName);
            ClassUtils.makeAccessible(method);
            return method.invoke(annotation);
        } catch (Throwable ex) {
        }
        return null;
    }

}
