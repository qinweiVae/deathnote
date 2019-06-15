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

    /**
     * 首字母大写
     */
    public static String capitalize(String str) {
        return changeFirstCharacterCase(str, true);
    }

    /**
     * 首字母小写
     */
    public static String decapitalize(String str) {
        return changeFirstCharacterCase(str, false);
    }

    private static String changeFirstCharacterCase(String str, boolean capitalize) {
        if (isEmpty(str)) {
            return str;
        }
        char original = str.charAt(0);
        char update;
        if (capitalize) {
            update = Character.toUpperCase(original);
        } else {
            update = Character.toLowerCase(original);
        }
        if (original == update) {
            return str;
        }
        char[] chars = str.toCharArray();
        chars[0] = update;
        return new String(chars, 0, chars.length);
    }

    /**
     * 查找字符串中指定字符出现的次数
     */
    public static int countMatchString(String str, String strToMatch) {
        int count = 0;
        String substring = str.trim();
        int index = 0;
        while (index != -1) {
            index = substring.indexOf(strToMatch);
            if (index != -1) {
                count++;
                substring = substring.substring(index + strToMatch.length());
            }
        }
        return count;
    }

    /**
     * 把数组转成字符串，用指定的限定符分割
     */
    public static String arrayToDelimitedString(Object[] array, String delimited) {
        if (ObjectUtils.isEmpty(array)) {
            return "";
        }
        if (array.length == 1) {
            return String.valueOf(array[0]);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(delimited);
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }
}
