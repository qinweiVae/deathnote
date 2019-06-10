package com.qinwei.deathnote.utils;

import com.qinwei.deathnote.beans.bean.CachedIntrospectionResults;
import com.qinwei.deathnote.support.convert.Conversion;
import com.qinwei.deathnote.support.convert.DefaultConversion;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author qinwei
 * @date 2019-05-29
 */
public class BeanUtils {

    public static void copyProperties(Object source, Object target) {
        copyProperties(source, target, null);
    }

    /**
     * source必须有getter方法,target必须有对应的setter方法,且属性类型相同  才能copy
     * 如果有对应的类型转换器，类型不同也能copy
     */
    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
        assert source != null : "source can not be null";
        assert target != null : "target can not be null";
        //需要忽略的属性
        List<String> ignoreList = Optional.ofNullable(ignoreProperties).map(Arrays::asList).orElse(null);

        Conversion conversion = DefaultConversion.getInstance();

        PropertyDescriptor[] targetPds = getPropertyDescriptors(target.getClass());
        for (PropertyDescriptor targetPd : targetPds) {
            Method setter = targetPd.getWriteMethod();
            if (setter != null && (ignoreList == null || !ignoreList.contains(targetPd.getName()))) {
                PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
                if (sourcePd != null) {
                    Method getter = sourcePd.getReadMethod();
                    Class<?> parameterType = setter.getParameterTypes()[0];
                    if (getter != null &&
                            (ClassUtils.isAssignable(parameterType, getter.getReturnType()) || conversion.canConvert(getter.getReturnType(), parameterType))) {
                        try {
                            if (!Modifier.isPublic(getter.getDeclaringClass().getModifiers())) {
                                getter.setAccessible(true);
                            }
                            //调用source 的 getter方法
                            Object value = getter.invoke(source);
                            if (!Modifier.isPublic(setter.getDeclaringClass().getModifiers())) {
                                setter.setAccessible(true);
                            }
                            //调用target 的 setter方法
                            setter.invoke(target, conversion.convertIfNecessary(value, parameterType));
                        } catch (Exception e) {
                            throw new IllegalStateException("Could not copy property '" + targetPd.getName() + "' from source to target", e);
                        }
                    }
                }
            }
        }
    }

    public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) {
        CachedIntrospectionResults cr = CachedIntrospectionResults.forClass(clazz);
        return cr.getPropertyDescriptors();
    }

    public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) {
        CachedIntrospectionResults cr = CachedIntrospectionResults.forClass(clazz);
        return cr.getPropertyDescriptor(propertyName);
    }

}
