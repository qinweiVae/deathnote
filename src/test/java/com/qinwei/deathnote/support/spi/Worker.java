package com.qinwei.deathnote.support.spi;

/**
 * @author qinwei
 * @date 2019-05-21
 */
@SPI("male")
public interface Worker {

    void work();

    String getName();
}
