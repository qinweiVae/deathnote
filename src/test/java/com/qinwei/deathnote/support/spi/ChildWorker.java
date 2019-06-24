package com.qinwei.deathnote.support.spi;

import com.qinwei.deathnote.context.annotation.Component;

/**
 * @author qinwei
 * @date 2019-05-21
 */
@Component
public class ChildWorker implements Worker {

    @Override
    public void work() {
        System.out.println("child worker ...");
    }
}
