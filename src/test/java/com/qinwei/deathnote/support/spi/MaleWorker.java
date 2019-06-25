package com.qinwei.deathnote.support.spi;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author qinwei
 * @date 2019-05-21
 */
@Slf4j
public class MaleWorker implements Worker {

    private String name;

    public MaleWorker(String name, int a, boolean flag) {
        log.info(name + a + flag);
    }

    public MaleWorker() {
    }

    private MaleWorker(String name, int a, boolean flag, List list) {
        log.info(name);
    }

    MaleWorker(String name) {
        log.info(name);
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
        log.info("male worker ..." + name);
    }

    public void stop() {
        log.info("male worker close ..." + name);
    }
}
