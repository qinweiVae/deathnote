package com.qinwei.deathnote.config;

import com.qinwei.deathnote.utils.StringUtils;

import java.util.Map;

/**
 * @author qinwei
 * @date 2019-05-09
 */
public interface Config {

    void initConfig();

    boolean containsProperty(String key);

    Map<String, Object> getProperties();

    String getProperty(String key);

    default String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return StringUtils.isEmpty(value) ? defaultValue : value;
    }

    <T> T getProperty(String key, Class<T> targetType);

    default <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        T value = getProperty(key, targetType);
        return value == null ? defaultValue : value;
    }

}
