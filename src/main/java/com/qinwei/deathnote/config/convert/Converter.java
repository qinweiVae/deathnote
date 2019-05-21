package com.qinwei.deathnote.config.convert;

/**
 * @author qinwei
 * @date 2019-05-10
 */
public interface Converter<S, T> {

    T convert(S source);
}
