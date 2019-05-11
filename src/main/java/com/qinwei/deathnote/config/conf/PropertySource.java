package com.qinwei.deathnote.config.conf;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author qinwei
 * @date 2019-05-09
 */
@Getter
@Setter
public class PropertySource {

    private final ConfigType configType;

    private final Map<String, Object> source;

    public PropertySource(ConfigType configType, Map<String, Object> source) {
        this.configType = configType;
        this.source = source;
    }

    public boolean containsProperty(String name) {
        return source.containsKey(name);
    }

    public Object getProperty(String name) {
        return source.get(name);
    }
}
