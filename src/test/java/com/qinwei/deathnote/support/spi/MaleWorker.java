package com.qinwei.deathnote.support.spi;

import java.util.List;

/**
 * @author qinwei
 * @date 2019-05-21
 */
public class MaleWorker implements Worker {

    private String name;

    public MaleWorker(String name, int a, boolean flag) {
        System.out.println(name + a + flag);
    }

    public MaleWorker() {
    }

    private MaleWorker(String name, int a, boolean flag, List list) {
        System.out.println(name);
    }

    MaleWorker(String name) {
        System.out.println(name);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void work() {
        System.out.println("male worker ..." + name);
    }

    public void stop() {
        System.out.println("male worker close ..." + name);
    }
}
