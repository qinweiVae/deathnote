package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.utils.AnnotationUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author qinwei
 * @date 2019-06-11
 */
public class AnnotationOrderComparator implements Comparator {

    public static final AnnotationOrderComparator INSTANCE = new AnnotationOrderComparator();

    @Override
    public int compare(Object o1, Object o2) {
        int i1 = getOrder(o1);
        int i2 = getOrder(o2);
        return Integer.compare(i1, i2);
    }

    protected int getOrder(Object obj) {
        if (obj != null) {
            Integer order = findOrder(obj);
            if (order != null) {
                return order;
            }
        }
        return Integer.MAX_VALUE;
    }

    protected Integer findOrder(Object obj) {
        Order order;
        if (obj instanceof Class) {
            order = AnnotationUtils.findAnnotation((Class<?>) obj, Order.class);
        } else if (obj instanceof Method) {
            order = AnnotationUtils.findAnnotation((Method) obj, Order.class);
        } else if (obj instanceof AnnotatedElement) {
            order = AnnotationUtils.findAnnotation((AnnotatedElement) obj, Order.class);
        } else {
            order = AnnotationUtils.findAnnotation(obj.getClass(), Order.class);
        }
        if (order != null) {
            return order.value();
        }
        return null;
    }

    public static void sort(List<?> list) {
        if (list.size() > 1) {
            list.sort(INSTANCE);
        }
    }

    public static void sort(Object[] array) {
        if (array.length > 1) {
            Arrays.sort(array, INSTANCE);
        }
    }
}
