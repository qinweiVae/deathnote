package com.qinwei.deathnote.beans.bean;

import com.qinwei.deathnote.utils.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public abstract class AttributeAccessorSupport implements AttributeAccessor {

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    @Override
    public void setAttribute(String name, Object value) {
        if (value != null) {
            this.attributes.put(name, value);
        } else {
            removeAttribute(name);
        }
    }

    @Override
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public Object removeAttribute(String name) {
        return this.attributes.remove(name);
    }

    @Override
    public boolean hasAttribute(String name) {
        return this.attributes.containsKey(name);
    }

    @Override
    public String[] attributeNames() {
        return StringUtils.toArray(this.attributes.keySet());
    }
}
