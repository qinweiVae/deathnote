package com.qinwei.deathnote.utils;

/**
 * @author qinwei
 * @date 2019-05-17
 */
public class Holder<T> {

    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T t) {
        this.value = t;
    }
}
