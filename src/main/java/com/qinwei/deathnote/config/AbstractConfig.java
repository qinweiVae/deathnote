package com.qinwei.deathnote.config;


import com.qinwei.deathnote.support.convert.Conversion;
import com.qinwei.deathnote.support.convert.DefaultConversion;
import com.qinwei.deathnote.support.resolve.DefaultPropertyResolver;
import com.qinwei.deathnote.support.resolve.PropertyResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-05-09
 */
public abstract class AbstractConfig implements Config {

    private List<PropertySource> propertySources = new ArrayList<>();

    private Map<String, Object> config = new HashMap<>();

    private PropertyResolver propertyResolver = new DefaultPropertyResolver();

    protected void addPropertySource(PropertySource propertySource) {
        if (propertySource == null) {
            return;
        }
        propertySources.add(propertySource);
    }

    /**
     * 清除缓存
     */
    protected void clearConfig() {
        propertySources.clear();
        config.clear();
    }

    /**
     * 按照优先级排序,sort越低优先级越高
     * 解析占位符
     */
    protected void init() {
        propertySources.stream()
                .sorted((o1, o2) -> o1.getOrder() > o2.getOrder() ? -1 : 1)
                .forEach(propertySource -> config.putAll(propertySource.getSource()));

        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                String resolvedValue = propertyResolver.resolvePlaceholders((String) value, config);
                config.put(key, resolvedValue);
            }
        }
    }

    protected void setPropertyResolver(PropertyResolver propertyResolver) {
        this.propertyResolver = propertyResolver;
    }

    @Override
    public Map<String, Object> getProperties() {
        return config;
    }

    @Override
    public boolean containsProperty(String key) {
        return getProperties().containsKey(key);
    }

    @Override
    public String getProperty(String key) {
        Object value = getProperties().get(key);
        return value == null ? null : getConversion().convertIfNecessary(value, String.class);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        Object value = getProperties().get(key);
        return value == null ? null : getConversion().convertIfNecessary(value, targetType);
    }

    protected Conversion getConversion() {
        return DefaultConversion.getInstance();
    }

    public Map<String, Object> getSystemProperties() {
        return (Map) System.getProperties();
    }

    public Map<String, Object> getSystemEnvironment() {
        return (Map) System.getenv();
    }
}
