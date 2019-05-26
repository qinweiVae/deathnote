package com.qinwei.deathnote.beans.registry;

import com.qinwei.deathnote.utils.StringUtils;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinwei
 * @date 2019-05-23
 */
public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {

    private final Map<String, Object> singletonInstances = new ConcurrentHashMap<String, Object>(256);

    private final Set<String> registeredNames = new LinkedHashSet<>(256);


    @Override
    public void registerSingleton(String beanName, Object singletonObject) {
        assert !StringUtils.isEmpty(beanName) : "Bean name must not be null";
        assert singletonObject != null : "Singleton object must not be null";
        Object registeredSingleton = singletonInstances.get(beanName);
        if (registeredSingleton != null) {
            throw new IllegalStateException("Could not register object [" + singletonObject +
                    "] under bean name '" + beanName + "': there is already object [" + registeredSingleton + "] bound");
        }
        addSingleton(beanName, singletonObject);
    }

    private void addSingleton(String beanName, Object singletonObject) {
        synchronized (singletonInstances) {
            singletonInstances.put(beanName, singletonObject);
            registeredNames.add(beanName);
        }
    }

    @Override
    public Object getSingleton(String beanName) {
        return singletonInstances.get(beanName);
    }

    @Override
    public boolean containsSingleton(String beanName) {
        return singletonInstances.containsKey(beanName);
    }

    @Override
    public String[] getSingletonNames() {
        synchronized (singletonInstances) {
            return registeredNames.toArray(new String[registeredNames.size()]);
        }
    }

    @Override
    public int getSingletonCount() {
        synchronized (singletonInstances) {
            return registeredNames.size();
        }
    }

    public void destroySingletons() {

    }
}
