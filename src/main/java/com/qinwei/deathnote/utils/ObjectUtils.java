package com.qinwei.deathnote.utils;

import java.util.Arrays;

/**
 * @author qinwei
 * @date 2019-05-28
 */
public class ObjectUtils {

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean containsElement(Object[] array, Object element) {
        if (array == null) {
            return false;
        }
        return Arrays.stream(array).anyMatch(value -> value == element || value.equals(element));
    }

}
