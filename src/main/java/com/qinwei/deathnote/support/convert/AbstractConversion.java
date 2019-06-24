package com.qinwei.deathnote.support.convert;

import com.qinwei.deathnote.context.support.ResolveType;
import com.qinwei.deathnote.utils.ClassUtils;

import java.util.ArrayList;
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
            ResolveType resolveType = ResolveType.forType(converter.getClass());
            convertCache.add(new TypeDescriptor(resolveType.resolveGeneric(0), resolveType.resolveGeneric(1), converter));
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

    @Override
    public <T> T convertIfNecessary(Object source, Class<T> targetType) {
        if (source == null) {
            return null;
        }
        if (ClassUtils.isAssignable(targetType, source.getClass()) || targetType.isInstance(source)) {
            return (T) source;
        }
        return convert(source, targetType);
    }

}
