package com.qinwei.deathnote.beans.bean;


/**
 * @author qinwei
 * @date 2019-06-27
 */
public interface AttributeAccessor {

    void setAttribute(String name, Object value);

    Object getAttribute(String name);

    Object removeAttribute(String name);

    boolean hasAttribute(String name);

    String[] attributeNames();
}
