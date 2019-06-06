package com.qinwei.deathnote.beans.bean;

import com.qinwei.deathnote.utils.ClassUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-05-29
 */
public class BeanWrapperImpl implements BeanWrapper {

    private Object wrappedObject;

    private CachedIntrospectionResults cachedIntrospectionResults;

    public BeanWrapperImpl() {
    }

    public BeanWrapperImpl(Object object) {
        setBeanInstance(object);
    }

    public BeanWrapperImpl(Class<?> clazz) {
        setBeanInstance(ClassUtils.instantiateClass(clazz));
    }

    public void setBeanInstance(Object obj) {
        this.wrappedObject = obj;
    }

    @Override
    public Object getWrappedInstance() {
        return this.wrappedObject;
    }

    @Override
    public Class<?> getWrappedClass() {
        return getWrappedInstance().getClass();
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        return getCachedIntrospectionResults().getPropertyDescriptors();
    }

    @Override
    public PropertyDescriptor getPropertyDescriptor(String propertyName) {
        return getCachedIntrospectionResults().getPropertyDescriptor(propertyName);
    }

    @Override
    public void setPropertyValues(Map<String, Object> propertyValue) {
        for (Map.Entry<String, Object> entry : propertyValue.entrySet()) {
            String propertyName = entry.getKey();
            PropertyDescriptor pd = getPropertyDescriptor(propertyName);
            Method writeMethod = pd.getWriteMethod();
            ClassUtils.makeAccessible(writeMethod);
            try {
                writeMethod.invoke(getWrappedInstance(), entry.getValue());
            } catch (Exception e) {
                throw new RuntimeException("Unable to invoke setter , method name : " + pd.getWriteMethod().getName());
            }
        }
    }

    private CachedIntrospectionResults getCachedIntrospectionResults() {
        if (this.cachedIntrospectionResults == null) {
            this.cachedIntrospectionResults = CachedIntrospectionResults.forClass(getWrappedClass());
        }
        return this.cachedIntrospectionResults;
    }
}
