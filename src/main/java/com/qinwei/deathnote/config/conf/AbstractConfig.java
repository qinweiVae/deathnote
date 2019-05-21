package com.qinwei.deathnote.config.conf;


import com.qinwei.deathnote.config.convert.Conversion;
import com.qinwei.deathnote.config.convert.DefaultConversion;
import com.qinwei.deathnote.utils.ClassUtils;

import java.util.ArrayList;
import java.util.Comparator;
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
        if (propertySource == null) {
            return;
        }
        propertySources.add(propertySource);
    }

    protected void clearPropertySource() {
        propertySources.clear();
    }

    /**
     * REMOTE 拥有高优先级
     */
    protected void sortByOrder() {
        propertySources.sort(Comparator.comparing(PropertySource::getOrder));
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
                //返回第一个匹配到的值
                .findFirst()
                .map(Object::toString)
                .orElse(null);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        return propertySources.stream()
                .map(propertySource -> propertySource.getProperty(key))
                .filter(Objects::nonNull)
                //返回第一个匹配到的值
                .findFirst()
                .map(value -> {
                    //如果可以强转
                    if (ClassUtils.isAssignable(targetType, value.getClass())) {
                        return (T) value;
                    }
                    //不能强转的话，创建转换器进行类型转换
                    Conversion conversion = DefaultConversion.getInstance();
                    return conversion.convert(value, targetType);
                })
                .orElse(null);
    }

    public Map<String, Object> getSystemProperties() {
        return (Map) System.getProperties();
    }

    public Map<String, Object> getSystemEnvironment() {
        return (Map) System.getenv();
    }
}
