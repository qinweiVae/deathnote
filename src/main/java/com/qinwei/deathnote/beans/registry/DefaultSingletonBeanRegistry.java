package com.qinwei.deathnote.beans.registry;

import com.qinwei.deathnote.beans.bean.DisposableBean;
import com.qinwei.deathnote.utils.CollectionUtils;
import com.qinwei.deathnote.utils.StringUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinwei
 * @date 2019-05-23
 */
public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {

    /**
     * beanName ---> 单例
     */
    private final Map<String, Object> singletonInstances = new ConcurrentHashMap<>(256);

    /**
     * 所有注册的 beanNames
     */
    private final Set<String> registeredNames = new LinkedHashSet<>(256);

    /**
     * beanName ---> DisposableBean
     */
    private final Map<String, Object> disposableBeans = new LinkedHashMap<>();

    /**
     * beanName ---> beanName依赖的所有 beanNames
     */
    private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<>(64);

    /**
     * beanName --->所有依赖beanName的beanNames
     */
    private final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<>(64);


    public void registerDependentBean(String beanName, String dependentBeanName) {
        String canonicalName = super.canonicalName(beanName);
        synchronized (this.dependentBeanMap) {
            Set<String> dependentBeans = this.dependentBeanMap.computeIfAbsent(canonicalName, k -> new LinkedHashSet<>(8));
            dependentBeans.add(dependentBeanName);
        }
        synchronized (this.dependenciesForBeanMap) {
            Set<String> dependenciesForBean = this.dependenciesForBeanMap.computeIfAbsent(dependentBeanName, k -> new LinkedHashSet<>(8));
            dependenciesForBean.add(canonicalName);
        }
    }

    public String[] getDependentBeans(String beanName) {
        Set<String> dependentBeans = this.dependentBeanMap.get(beanName);
        if (dependentBeans == null) {
            return new String[0];
        }
        synchronized (this.dependentBeanMap) {
            return StringUtils.toArray(dependentBeans);
        }
    }

    public String[] getDependenciesForBean(String beanName) {
        Set<String> dependenciesForBean = this.dependenciesForBeanMap.get(beanName);
        if (dependenciesForBean == null) {
            return new String[0];
        }
        synchronized (this.dependenciesForBeanMap) {
            return StringUtils.toArray(dependenciesForBean);
        }
    }

    protected boolean isDependent(String beanName, String dependentBeanName) {
        synchronized (this.dependentBeanMap) {
            return isDependent(beanName, dependentBeanName, null);
        }
    }

    private boolean isDependent(String beanName, String dependentBeanName, Set<String> alreadySeen) {
        if (alreadySeen != null && alreadySeen.contains(beanName)) {
            return false;
        }
        String canonicalName = canonicalName(beanName);
        Set<String> dependentBeans = this.dependentBeanMap.get(canonicalName);
        if (dependentBeans == null) {
            return false;
        }
        if (dependentBeans.contains(dependentBeanName)) {
            return true;
        }
        for (String transitiveDependency : dependentBeans) {
            if (alreadySeen == null) {
                alreadySeen = new HashSet<>();
            }
            alreadySeen.add(beanName);
            if (isDependent(transitiveDependency, dependentBeanName, alreadySeen)) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasDependentBean(String beanName) {
        return this.dependentBeanMap.containsKey(beanName);
    }

    @Override
    public void registerSingleton(String beanName, Object singletonObject) {
        assert !StringUtils.isEmpty(beanName) : "Bean name must not be null";
        assert singletonObject != null : "Singleton object must not be null";
        Object registeredSingleton = this.singletonInstances.get(beanName);
        if (registeredSingleton != null) {
            throw new IllegalStateException("Could not register object [" + singletonObject +
                    "] under bean name '" + beanName + "': there is already object [" + registeredSingleton + "] bound");
        }
        addSingleton(beanName, singletonObject);
    }

    private void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonInstances) {
            this.singletonInstances.put(beanName, singletonObject);
            this.registeredNames.add(beanName);
        }
    }

    @Override
    public Object getSingleton(String beanName) {
        return this.singletonInstances.get(beanName);
    }

    @Override
    public boolean containsSingleton(String beanName) {
        return this.singletonInstances.containsKey(beanName);
    }

    @Override
    public String[] getSingletonNames() {
        synchronized (this.singletonInstances) {
            return StringUtils.toArray(this.registeredNames);
        }
    }

    @Override
    public int getSingletonCount() {
        synchronized (singletonInstances) {
            return this.registeredNames.size();
        }
    }

    public void destroySingletons() {
        Set<String> disposableBeanNames;
        synchronized (this.disposableBeans) {
            disposableBeanNames = this.disposableBeans.keySet();
        }
        for (String beanName : disposableBeanNames) {
            destroySingleton(beanName);
        }
        this.dependentBeanMap.clear();
        this.dependenciesForBeanMap.clear();
        clearSingletonCache();
    }

    protected void clearSingletonCache() {
        synchronized (this.singletonInstances) {
            this.singletonInstances.clear();
            this.registeredNames.clear();
        }
    }

    public void registerDisposableBean(String beanName, DisposableBean bean) {
        synchronized (this.disposableBeans) {
            this.disposableBeans.put(beanName, bean);
        }
    }

    public void destroySingleton(String beanName) {
        removeSingleton(beanName);
        DisposableBean disposableBean;
        synchronized (this.disposableBeans) {
            disposableBean = (DisposableBean) this.disposableBeans.remove(beanName);
        }
        destroyBean(beanName, disposableBean);
    }

    protected void destroyBean(String beanName, DisposableBean disposableBean) {
        Set<String> dependencies;
        synchronized (this.dependentBeanMap) {
            dependencies = this.dependentBeanMap.remove(beanName);
        }
        if (CollectionUtils.isNotEmpty(dependencies)) {
            for (String dependentBeanName : dependencies) {
                destroySingleton(dependentBeanName);
            }
        }
        if (disposableBean != null) {
            disposableBean.destroy();
        }
        synchronized (this.dependentBeanMap) {
            for (Iterator<Map.Entry<String, Set<String>>> iterator = this.dependentBeanMap.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, Set<String>> entry = iterator.next();
                Set<String> dependenciesToClean = entry.getValue();
                dependenciesToClean.remove(beanName);
                if (dependenciesToClean.isEmpty()) {
                    iterator.remove();
                }
            }
        }
        this.dependenciesForBeanMap.remove(beanName);
    }

    protected void removeSingleton(String beanName) {
        synchronized (this.singletonInstances) {
            this.singletonInstances.remove(beanName);
            this.registeredNames.remove(beanName);
        }
    }

}
