package com.qinwei.deathnote.support.convert;

import com.qinwei.deathnote.utils.ClassUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author qinwei
 * @date 2019-05-10
 */
public class AbstractConversion implements Conversion {

    private List<Converter> converters = new ArrayList<>();

    private List<TypeDescriptor> convertCache = new ArrayList<>();

    public void addConvert(Converter converter) {
        if (converter == null) {
            return;
        }
        converters.add(converter);
    }

    protected void initCache() {
        for (Converter converter : converters) {
            Type[] interfaces = converter.getClass().getGenericInterfaces();
            Arrays.stream(interfaces).
                    filter(type -> type instanceof ParameterizedType)
                    .map(type -> (ParameterizedType) type)
                    .filter(parameterizedType -> ClassUtils.isAssignable(Converter.class, (Class<?>) parameterizedType.getRawType()))
                    .map(ParameterizedType::getActualTypeArguments)
                    .forEach(arguments -> {
                        Class sourceCls = (Class) arguments[0];
                        Class targetCls = (Class) arguments[1];
                        convertCache.add(new TypeDescriptor(targetCls, sourceCls, converter));
                    });
        }
    }

    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return convertCache.stream().anyMatch(typeDescriptor -> typeDescriptor.getSourceType() == sourceType && typeDescriptor.getTargetType() == targetType);
    }

    @Override
    public <T> T convert(Object source, Class<T> targetType) {
        if (source == null) {
            return null;
        }
        return convertCache.stream()
                .filter(typeDescriptor -> typeDescriptor.getSourceType() == source.getClass() && typeDescriptor.getTargetType() == targetType)
                .findFirst()
                .map(typeDescriptor -> (T) typeDescriptor.getConverter().convert(source))
                .orElse(null);
    }

}
