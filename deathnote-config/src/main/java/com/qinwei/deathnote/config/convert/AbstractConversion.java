package com.qinwei.deathnote.config.convert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qinwei
 * @date 2019-05-10
 */
public class AbstractConversion implements Conversion {

    private List<Converter> converters = new ArrayList<>();

    public void addConvert(Converter converter) {
        converters.add(converter);
    }

    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return false;
    }

    @Override
    public <T> T convert(Object source, Class<T> targetType) {
        return null;
    }
}
