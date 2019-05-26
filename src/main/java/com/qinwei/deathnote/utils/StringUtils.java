package com.qinwei.deathnote.utils;

import java.util.Collection;

/**
 * @author qinwei
 * @date 2019-05-25 15:16
 */
public class StringUtils {

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim()) || "null".equalsIgnoreCase(str.trim());
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static String[] toArray(Collection<String> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException("collection must not be null ...");
        }
        return collection.toArray(new String[collection.size()]);
    }

}
