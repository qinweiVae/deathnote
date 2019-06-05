package com.qinwei.deathnote.beans.registry;

import com.qinwei.deathnote.beans.bean.DisposableBean;
import com.qinwei.deathnote.beans.factory.ObjectFactory;
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
     * beanName ---> 依赖beanName的所有beanName
     */
    private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<>(64);

    /**
     * beanName --->beanName的所有依赖beanNames
     */
    private final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<>(64);


    /**
     * 注册bean的依赖关系
     * <br> dependentBeanName 依赖于 beanName
     */
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

    /**
     * 获取依赖beanName的所有bean
     */
    public String[] getDependentBeans(String beanName) {
        Set<String> dependentBeans = this.dependentBeanMap.get(beanName);
        if (dependentBeans == null) {
            return new String[0];
        }
        synchronized (this.dependentBeanMap) {
            return StringUtils.toArray(dependentBeans);
        }
    }

    /**
     * 获取beanName 的所有依赖bean
     */
    public String[] getDependenciesForBean(String beanName) {
        Set<String> dependenciesForBean = this.dependenciesForBeanMap.get(beanName);
        if (dependenciesForBean == null) {
            return new String[0];
        }
        synchronized (this.dependenciesForBeanMap) {
            return StringUtils.toArray(dependenciesForBean);
        }
    }

    /**
     * 判断 dependentBeanName 是否依赖于 beanName
     */
    public boolean isDependent(String beanName, String dependentBeanName) {
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

    /**
     * 注册单例
     */
    @Override
    public void registerSingleton(String beanName, Object singletonObject) {
        assert !StringUtils.isEmpty(beanName) : "Bean name must not be null";
        assert singletonObject != null : "Singleton object must not be null";
        Object registeredSingleton = this.singletonInstances.get(beanName);
        if (registeredSingleton != null) {
            throw new IllegalStateException("Could not register object [" + singletonObject +
                    "] under beans name '" + beanName + "': there is already object [" + registeredSingleton + "] bound");
        }
        addSingleton(beanName, singletonObject);
    }

    private void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonInstances) {
            this.singletonInstances.put(beanName, singletonObject);
            this.registeredNames.add(beanName);
        }
    }

    /**
     * 根据 beanName 获取单例
     */
    @Override
    public Object getSingleton(String beanName) {
        return this.singletonInstances.get(beanName);
    }

    @Override
    public boolean containsSingleton(String beanName) {
        return this.singletonInstances.containsKey(beanName);
    }

    /**
     * 获取所有的单例的name
     */
    @Override
    public String[] getSingletonNames() {
        synchronized (this.singletonInstances) {
            return StringUtils.toArray(this.registeredNames);
        }
    }

    /**
     * 获取单例的数量
     */
    @Override
    public int getSingletonCount() {
        synchronized (singletonInstances) {
            return this.registeredNames.size();
        }
    }

    /**
     * 销毁所有单例
     */
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

    /**
     * 清理单例的缓存
     */
    protected void clearSingletonCache() {
        synchronized (this.singletonInstances) {
            this.singletonInstances.clear();
            this.registeredNames.clear();
        }
    }

    /**
     * 注册DisposableBean
     */
    public void registerDisposableBean(String beanName, DisposableBean bean) {
        synchronized (this.disposableBeans) {
            this.disposableBeans.put(beanName, bean);
        }
    }

    /**
     * 销毁单例
     */
    public void destroySingleton(String beanName) {
        removeSingleton(beanName);
        DisposableBean disposableBean;
        synchronized (this.disposableBeans) {
            disposableBean = (DisposableBean) this.disposableBeans.remove(beanName);
        }
        destroyBean(beanName, disposableBean);
    }

    /**
     * 移除所有依赖beanName的bean及beanName依赖的所有bean
     * 如果是DisposableBean，执行其 destroy() 方法
     */
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

    /**
     * 移除单例
     */
    protected void removeSingleton(String beanName) {
        synchronized (this.singletonInstances) {
            this.singletonInstances.remove(beanName);
            this.registeredNames.remove(beanName);
        }
    }

    /**
     * 已经注册单例直接返回，不存在创建完成后注册并返回
     */
    public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        assert !StringUtils.isEmpty(beanName) : "beans name must not be null";
        synchronized (this.singletonInstances) {
            Object singleton = this.singletonInstances.get(beanName);
            if (singleton == null) {
                beforeSingletonCreation(beanName);
                boolean newSingleton = false;
                try {
                    singleton = singletonFactory.getObject();
                    newSingleton = true;
                } catch (Exception e) {
                    singleton = this.singletonInstances.get(beanName);
                    if (singleton == null) {
                        throw e;
                    }
                } finally {
                    afterSingletonCreation(beanName);
                }
                if (newSingleton) {
                    addSingleton(beanName, singleton);
                }
            }
            return singleton;
        }
    }


    /**
     * 初始化单例前
     */
    protected void beforeSingletonCreation(String beanName) {

    }

    /**
     * 初始化单例后
     */
    protected void afterSingletonCreation(String beanName) {

    }
}
