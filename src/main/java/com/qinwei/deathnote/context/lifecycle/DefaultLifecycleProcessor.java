package com.qinwei.deathnote.context.lifecycle;

/**
 * @author qinwei
 * @date 2019-06-11
 */
public class DefaultLifecycleProcessor implements LifecycleProcessor {

    private volatile boolean running;

    @Override
    public void onRefresh() {
        this.running = true;
    }

    @Override
    public void onClose() {
        this.running = false;
    }

    @Override
    public void start() {
        this.running = true;
    }

    @Override
    public void stop() {
        this.running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

}
