package com.qinwei.deathnote.beans.bean;

import java.beans.PropertyDescriptor;

/**
 * @author qinwei
 * @date 2019-05-28
 */
public interface BeanWrapper {

    Object getWrappedInstance();

    Class<?> getWrappedClass();

    PropertyDescriptor[] getPropertyDescriptors();

    PropertyDescriptor getPropertyDescriptor(String propertyName);
}
