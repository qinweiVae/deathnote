package com.qinwei.deathnote.aop.support;

import com.qinwei.deathnote.support.convert.Converter;

import java.util.Comparator;

/**
 * @author qinwei
 * @date 2019-07-02
 */
public class ConvertingComparator<S, T> implements Comparator<S> {

    private final Comparator<T> comparator;

    private final Converter<S, T> converter;

    public ConvertingComparator(Comparator<T> comparator, Converter<S, T> converter) {
        assert comparator != null : "comparator must not be null";
        assert converter != null : "converter must not be null";
        this.comparator = comparator;
        this.converter = converter;
    }

    @Override
    public int compare(S o1, S o2) {
        T c1 = this.converter.convert(o1);
        T c2 = this.converter.convert(o2);
        return this.comparator.compare(c1, c2);
    }
}
