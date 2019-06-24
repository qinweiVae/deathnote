package com.qinwei.deathnote.support.spi;

/**
 * @author qinwei
 * @date 2019-05-21
 */
public class FemaleWorker implements Worker {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void work() {
        System.out.println("female  worker ..." + name);
    }

    public void stop() {
        System.out.println("female worker close ..." + name);
    }
}
