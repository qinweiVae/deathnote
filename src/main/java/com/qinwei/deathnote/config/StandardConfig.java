package com.qinwei.deathnote.config;

import com.qinwei.deathnote.support.scan.ResourcesScanner;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author qinwei
 * @date 2019-05-09
 */
@Slf4j
public class StandardConfig extends AbstractConfig {

    private static final AtomicReference<StandardConfig> INSTANCE = new AtomicReference();

    private final Object monitor = new Object();

    public static final StandardConfig getInstance() {
        for (; ; ) {
            StandardConfig current = INSTANCE.get();
            if (current != null) {
                return current;
            }
            current = new StandardConfig();
            if (INSTANCE.compareAndSet(null, current)) {
                return current;
            }
        }
    }

    private StandardConfig() {
    }

    @Override
    public void initConfig() {
        synchronized (monitor) {
            log.info("正在初始化 config ...");
            clearConfig();
            addPropertySource(new PropertySource(1, getSystemProperties()));
            addPropertySource(new PropertySource(2, getSystemEnvironment()));
            //配置文件
            addPropertySource(new PropertySource(3, ResourcesScanner.getInstance().scan()));
            init();
        }
    }

}
