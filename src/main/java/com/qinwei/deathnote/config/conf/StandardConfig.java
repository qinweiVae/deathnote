package com.qinwei.deathnote.config.conf;

import com.qinwei.deathnote.support.scan.ResourcesScanner;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author qinwei
 * @date 2019-05-09
 */
public class StandardConfig extends AbstractConfig {

    private static final AtomicReference<StandardConfig> INSTANCE = new AtomicReference();

    private final Object lock = new Object();

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
        initConfig();
    }

    public void addPropertySources(PropertySource... propertySources) {
        for (PropertySource propertySource : propertySources) {
            addPropertySource(propertySource);
        }
        sortByOrder();
    }

    public void initConfig() {
        synchronized (lock) {
            clearPropertySource();
            addPropertySource(new PropertySource(1, getSystemProperties()));
            addPropertySource(new PropertySource(2, getSystemEnvironment()));
            //配置文件
            addPropertySource(new PropertySource(3, ResourcesScanner.getInstance().scan()));
            sortByOrder();
        }
    }

}
