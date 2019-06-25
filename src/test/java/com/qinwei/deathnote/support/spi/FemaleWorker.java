package com.qinwei.deathnote.support.spi;

import lombok.extern.slf4j.Slf4j;

/**
 * @author qinwei
 * @date 2019-05-21
 */
@Slf4j
public class FemaleWorker implements Worker {

    private String name;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void work() {
        log.info("female  worker ..." + name);
    }

    public void stop() {
        log.info("female worker close ..." + name + " --- stop()");
    }

    public void shutdown() {
        log.info("female worker close ..." + name + " --- shutdown()");
    }

    public void close() {
        log.info("female worker close ..." + name + " --- close()");
    }
}
