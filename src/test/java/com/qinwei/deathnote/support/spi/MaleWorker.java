package com.qinwei.deathnote.support.spi;

/**
 * @author qinwei
 * @date 2019-05-21
 */
public class MaleWorker implements Worker {

    @Override
    public void work() {
        System.out.println("male worker ...");
    }
}
