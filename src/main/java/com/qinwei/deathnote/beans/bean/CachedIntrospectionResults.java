package com.qinwei.deathnote.beans.bean;

import com.qinwei.deathnote.utils.StringUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinwei
 * @date 2019-05-29
 */
public class CachedIntrospectionResults {

    private static final Map<Class<?>, CachedIntrospectionResults> CLASS_CACHE = new ConcurrentHashMap<>(64);

    private final BeanInfo beanInfo;

    private final Map<String, PropertyDescriptor> propertyDescriptorCache;

    private CachedIntrospectionResults(Class<?> beanClass) {
        assert beanClass != null : "bean class can not be null";
        this.propertyDescriptorCache = new LinkedHashMap<>();
        try {
            this.beanInfo = Introspector.getBeanInfo(beanClass);
            //获取的属性不是类的成员变量,只要存在setter/getter 例如 getAbc 或者 setAbc 方法,属性就是 abc
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
            //所有的类默认是继承object类的，而object类中含有getClass方法，所以class也是一个属性
            Arrays.stream(pds)
                    .filter(pd -> !"class".equals(pd.getName()))
                    .forEach(pd -> this.propertyDescriptorCache.put(pd.getName(), pd));
            //递归查询实现的接口是否含有 setter/getter
            Class<?> currentClass = beanClass;
            while (currentClass != null && currentClass != Object.class) {
                introspectInterfaces(currentClass);
                currentClass = currentClass.getSuperclass();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to get bean info");
        }
    }

    private void introspectInterfaces(Class<?> currentClass) throws IntrospectionException {
        for (Class<?> ifc : currentClass.getInterfaces()) {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(ifc).getPropertyDescriptors()) {
                PropertyDescriptor existingPd = this.propertyDescriptorCache.get(pd.getName());
                if (existingPd == null || (existingPd.getReadMethod() == null && pd.getReadMethod() != null)) {
                    this.propertyDescriptorCache.put(pd.getName(), pd);
                }
            }
            introspectInterfaces(ifc);
        }
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        return this.propertyDescriptorCache.values().toArray(new PropertyDescriptor[this.propertyDescriptorCache.size()]);
    }

    public Map<String, PropertyDescriptor> getPropertyDescriptorCache() {
        return propertyDescriptorCache;
    }

    public PropertyDescriptor getPropertyDescriptor(String propertyName) {
        PropertyDescriptor pd = this.propertyDescriptorCache.get(propertyName);
        if (pd == null && StringUtils.isNotEmpty(propertyName)) {
            pd = this.propertyDescriptorCache.get(StringUtils.decapitalize(propertyName));
            if (pd == null) {
                pd = this.propertyDescriptorCache.get(StringUtils.capitalize(propertyName));
            }
        }
        return pd;
    }

    public BeanInfo getBeanInfo() {
        return this.beanInfo;
    }

    public Class<?> getBeanClass() {
        return this.beanInfo.getBeanDescriptor().getBeanClass();
    }

    public static CachedIntrospectionResults forClass(Class<?> beanClass) {
        CachedIntrospectionResults results = CLASS_CACHE.get(beanClass);
        if (results != null) {
            return results;
        }
        results = new CachedIntrospectionResults(beanClass);
        CLASS_CACHE.put(beanClass, results);
        return results;
    }

}
