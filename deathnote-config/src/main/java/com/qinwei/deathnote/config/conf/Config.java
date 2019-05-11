package com.qinwei.deathnote.config.conf;

import org.apache.commons.lang3.StringUtils;

/**
 * @author qinwei
 * @date 2019-05-09
 */
public interface Config {

    boolean containsProperty(String key);

    String getProperty(String key);

    default String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return StringUtils.isEmpty(value) ? defaultValue : value;
    }

    <T> T getProperty(String key, Class<T> targetType);

    <T> T getProperty(String key, Class<T> targetType, T defaultValue);

}
