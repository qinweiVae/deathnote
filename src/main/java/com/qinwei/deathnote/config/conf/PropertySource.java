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

    private int order = 1 << 10;

    private Map<String, Object> source;

    public PropertySource(Map<String, Object> source) {
        this.source = source;
    }

    public PropertySource(int order, Map<String, Object> source) {
        this.order = order;
        this.source = source;
    }

    public boolean containsProperty(String name) {
        return source.containsKey(name);
    }

    public Object getProperty(String name) {
        return source.get(name);
    }
}
