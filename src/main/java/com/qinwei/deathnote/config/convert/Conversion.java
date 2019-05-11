package com.qinwei.deathnote.config.convert;

/**
 * @author qinwei
 * @date 2019-05-10
 */
public interface Conversion {

    boolean canConvert(Class<?> sourceType, Class<?> targetType);

    <T> T convert(Object source, Class<T> targetType);
}
