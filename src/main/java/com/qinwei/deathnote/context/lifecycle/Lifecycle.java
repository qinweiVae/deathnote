package com.qinwei.deathnote.context.lifecycle;

/**
 * @author qinwei
 * @date 2019-05-21
 */
public interface Lifecycle {

    void start();

    void stop();

    boolean isRunning();
}
