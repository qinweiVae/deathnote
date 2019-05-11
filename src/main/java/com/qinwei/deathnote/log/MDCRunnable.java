package com.qinwei.deathnote.log;

import org.slf4j.MDC;

import java.util.Map;

/**
 * @author qinwei
 * @date 2019-05-09
 */
public class MDCRunnable implements Runnable {

    private final Runnable runnable;

    private final Map<String, String> map;

    public MDCRunnable(Runnable runnable) {
        this.runnable = runnable;
        this.map = MDC.getCopyOfContextMap();
    }

    @Override
    public void run() {
        // 传入已保存的MDC值
        map.entrySet().forEach(entry -> MDC.put(entry.getKey(), entry.getValue()));
        runnable.run();
        // 移除已保存的MDC值
        map.keySet().stream().forEach(MDC::remove);
    }
}
