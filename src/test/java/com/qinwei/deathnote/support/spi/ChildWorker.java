package com.qinwei.deathnote.support.spi;

/**
 * @author qinwei
 * @date 2019-05-21
 */
public class ChildWorker implements Worker {

    @Override
    public void work() {
        System.out.println("child worker ...");
    }
}
