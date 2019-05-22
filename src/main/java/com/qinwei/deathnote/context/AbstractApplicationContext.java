package com.qinwei.deathnote.context;

import com.qinwei.deathnote.context.event.ApplicationEvent;
import com.qinwei.deathnote.config.conf.Config;
import com.qinwei.deathnote.config.conf.StandardConfig;
import com.qinwei.deathnote.context.event.ApplicationListener;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public class AbstractApplicationContext implements ApplicationContext {

    @Override
    public Config getConfig() {
        return StandardConfig.getInstance();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void publishEvent(ApplicationEvent event) {

    }

    @Override
    public void publishEvent(Object event) {

    }

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {

    }

    @Override
    public void refresh() {

    }

    @Override
    public void registerShutdownHook() {

    }
}
