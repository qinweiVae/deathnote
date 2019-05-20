package com.qinwei.deathnote.config.convert;

import com.qinwei.deathnote.support.spi.SPI;

/**
 * @author qinwei
 * @date 2019-05-10
 */
@SPI("int")
public interface Converter<S, T> {

    T convert(S source);
}
