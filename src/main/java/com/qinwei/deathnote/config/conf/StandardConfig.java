package com.qinwei.deathnote.config.conf;

import com.qinwei.deathnote.support.scan.ResourcesScanner;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author qinwei
 * @date 2019-05-09
 */
public class StandardConfig extends AbstractConfig {

    private static final AtomicReference<StandardConfig> INSTANCE = new AtomicReference();

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
        addPropertySource(new PropertySource(ConfigType.LOCAL, getSystemProperties()));
        addPropertySource(new PropertySource(ConfigType.LOCAL, getSystemEnvironment()));
        //配置文件
        Map<String, Object> resources = ResourcesScanner.getInstance().scan();
        addPropertySources(new PropertySource(ConfigType.LOCAL, resources));
    }

    public void addPropertySources(PropertySource... propertySources) {
        for (PropertySource propertySource : propertySources) {
            addPropertySource(propertySource);
        }
    }

    public void initConfig() {
        sortByConfigType();
    }

}
