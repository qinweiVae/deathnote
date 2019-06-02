package com.qinwei.deathnote.support.convert;

/**
 * @author qinwei
 * @date 2019-05-10
 */
public interface Converter<S, T> {

    T convert(S source);
}
