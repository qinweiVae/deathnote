package com.qinwei.deathnote.config.conf;


import com.qinwei.deathnote.config.utils.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author qinwei
 * @date 2019-05-09
 */
public abstract class AbstractConfig implements Config {

    private List<PropertySource> propertySources = new ArrayList<>();

    protected void addPropertySource(PropertySource propertySource) {
        propertySources.add(propertySource);
    }

    @Override
    public boolean containsProperty(String key) {
        return propertySources.stream().anyMatch(propertySource -> propertySource.containsProperty(key));
    }

    @Override
    public String getProperty(String key) {
        return propertySources.stream()
                .map(propertySource -> propertySource.getProperty(key))
                .filter(Objects::nonNull)
                .findFirst()
                .map(Object::toString)
                .orElse(null);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        return propertySources.stream()
                .map(propertySource -> propertySource.getProperty(key))
                .filter(Objects::nonNull)
                .findFirst()
                .map(value -> ClassUtils.isAssignable(targetType, value.getClass()) ? (T) value
                        : ClassUtils.convert(value, targetType))
                .orElse(null);
    }

    public static void main(String[] args) {
        Object value = 2;
        System.out.println(ClassUtils.isAssignable(Long.class, Double.class));
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return null;
    }

    public Map<String, Object> getSystemProperties() {
        return (Map) System.getProperties();
    }

    public Map<String, Object> getSystemEnvironment() {
        return (Map) System.getenv();
    }
}
