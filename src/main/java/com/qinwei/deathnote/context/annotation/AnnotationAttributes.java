package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-06-14
 */
public class AnnotationAttributes extends LinkedHashMap<String, Object> {

    private final Class<? extends Annotation> annotationType;

    public AnnotationAttributes(Map<String, Object> map) {
        super(map);
        this.annotationType = null;
    }

    public AnnotationAttributes(AnnotationAttributes other) {
        super(other);
        this.annotationType = other.annotationType;
    }

    public AnnotationAttributes(Class<? extends Annotation> annotationType) {
        assert annotationType != null : "'annotationType' must not be null";
        this.annotationType = annotationType;
    }

    public Class<? extends Annotation> annotationType() {
        return this.annotationType;
    }

    public String getString(String attributeName) {
        return getRequiredAttribute(attributeName, String.class);
    }

    public String[] getStringArray(String attributeName) {
        return getRequiredAttribute(attributeName, String[].class);
    }

    public boolean getBoolean(String attributeName) {
        return getRequiredAttribute(attributeName, Boolean.class);
    }

    public <N extends Number> N getNumber(String attributeName) {
        return (N) getRequiredAttribute(attributeName, Number.class);
    }

    public <E extends Enum<?>> E getEnum(String attributeName) {
        return (E) getRequiredAttribute(attributeName, Enum.class);
    }

    public <T> Class<? extends T> getClass(String attributeName) {
        return getRequiredAttribute(attributeName, Class.class);
    }

    public Class<?>[] getClassArray(String attributeName) {
        return getRequiredAttribute(attributeName, Class[].class);
    }

    public AnnotationAttributes getAnnotation(String attributeName) {
        return getRequiredAttribute(attributeName, AnnotationAttributes.class);
    }

    public <A extends Annotation> A getAnnotation(String attributeName, Class<A> annotationType) {
        return getRequiredAttribute(attributeName, annotationType);
    }

    public AnnotationAttributes[] getAnnotationArray(String attributeName) {
        return getRequiredAttribute(attributeName, AnnotationAttributes[].class);
    }

    public <A extends Annotation> A[] getAnnotationArray(String attributeName, Class<A> annotationType) {
        Object array = Array.newInstance(annotationType, 0);
        return (A[]) getRequiredAttribute(attributeName, array.getClass());
    }

    private <T> T getRequiredAttribute(String attributeName, Class<T> expectedType) {
        assert attributeName != null : "'attributeName' must not be null or empty";
        Object value = get(attributeName);
        assert value != null : "Attribute '" + attributeName + "' not found in attributes ";
        //如果是数组的话
        if (!expectedType.isInstance(value) &&
                expectedType.isArray() && expectedType.getComponentType().isInstance(value)) {
            Object array = Array.newInstance(expectedType.getComponentType(), 1);
            Array.set(array, 0, value);
            value = array;
        }
        if (!expectedType.isInstance(value)) {
            throw new IllegalArgumentException("Attribute '" + attributeName + "' is of type " + value.getClass().getName() + ", but " + expectedType.getName() + " was expected in attributes ");
        }
        return (T) value;
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
        Object obj = get(key);
        if (obj == null) {
            obj = put(key, value);
        }
        return obj;
    }

    @Override
    public String toString() {
        Iterator<Map.Entry<String, Object>> entries = entrySet().iterator();
        StringBuilder sb = new StringBuilder("{");
        while (entries.hasNext()) {
            Map.Entry<String, Object> entry = entries.next();
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(valueToString(entry.getValue()));
            sb.append(entries.hasNext() ? ", " : "");
        }
        sb.append("}");
        return sb.toString();
    }

    private String valueToString(Object value) {
        if (value == this) {
            return "(this Map)";
        }
        if (value instanceof Object[]) {
            return "[" + StringUtils.arrayToDelimitedString((Object[]) value, ", ") + "]";
        }
        return String.valueOf(value);
    }

    public static AnnotationAttributes fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        if (map instanceof AnnotationAttributes) {
            return (AnnotationAttributes) map;
        }
        return new AnnotationAttributes(map);
    }

}
