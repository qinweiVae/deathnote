package com.qinwei.deathnote.aop.support;

import com.qinwei.deathnote.aop.Advised;
import com.qinwei.deathnote.aop.AdvisorChainFactory;
import com.qinwei.deathnote.aop.DefaultAdvisorChainFactory;
import com.qinwei.deathnote.aop.ProxyConfig;
import com.qinwei.deathnote.aop.aspectj.Advice;
import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.aspectj.DefaultPointcutAdvisor;
import com.qinwei.deathnote.aop.targetSource.EmptyTargetSource;
import com.qinwei.deathnote.aop.targetSource.SingletonTargetSource;
import com.qinwei.deathnote.aop.targetSource.TargetSource;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.CollectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public class AdvisedSupport extends ProxyConfig implements Advised {

    private boolean preFiltered = false;

    private TargetSource targetSource = EmptyTargetSource.INSTANCE;

    private AdvisorChainFactory advisorChainFactory = new DefaultAdvisorChainFactory();

    private transient Map<Method, List<Object>> methodCache;

    private List<Class<?>> interfaces = new ArrayList<>();

    private List<Advisor> advisors = new ArrayList<>();

    public AdvisedSupport() {
        this.methodCache = new ConcurrentHashMap<>(32);
    }

    /**
     * 获取 MethodInterceptor 的 调用链
     */
    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) {
        List<Object> cached = this.methodCache.get(method);
        if (cached == null) {
            //获取 MethodInterceptor 的 调用链
            cached = this.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(this, method, targetClass);
            this.methodCache.put(method, cached);
        }
        return cached;
    }

    public void setAdvisorChainFactory(AdvisorChainFactory advisorChainFactory) {
        this.advisorChainFactory = advisorChainFactory;
    }

    public AdvisorChainFactory getAdvisorChainFactory() {
        return this.advisorChainFactory;
    }

    public void setInterfaces(Class<?>... interfaces) {
        this.interfaces.clear();
        for (Class<?> ifc : interfaces) {
            addInterface(ifc);
        }
    }

    public void addInterface(Class<?> intf) {
        assert intf.isInterface() : "[" + intf.getName() + "] is not an interface";
        if (!this.interfaces.contains(intf)) {
            this.interfaces.add(intf);
            adviceChanged();
        }
    }

    public boolean removeInterface(Class<?> intf) {
        return this.interfaces.remove(intf);
    }

    @Override
    public Class<?>[] getProxiedInterfaces() {
        return this.interfaces.toArray(new Class[0]);
    }

    @Override
    public boolean isInterfaceProxied(Class<?> intf) {
        return this.interfaces.stream().anyMatch(proxyIntf -> ClassUtils.isAssignable(intf, proxyIntf));
    }

    protected void adviceChanged() {
        this.methodCache.clear();
    }

    public void setTarget(Object target) {
        setTargetSource(new SingletonTargetSource(target));
    }

    @Override
    public void setTargetSource(TargetSource targetSource) {
        if (targetSource != null) {
            this.targetSource = targetSource;
        }
    }

    @Override
    public TargetSource getTargetSource() {
        return this.targetSource;
    }

    @Override
    public Class<?> getTargetClass() {
        return this.targetSource.getTargetClass();
    }

    @Override
    public void setPreFiltered(boolean preFiltered) {
        this.preFiltered = preFiltered;
    }

    @Override
    public boolean isPreFiltered() {
        return this.preFiltered;
    }

    @Override
    public Advisor[] getAdvisors() {
        return this.advisors.toArray(new Advisor[0]);
    }

    @Override
    public void addAdvisor(Advisor advisor) {
        int pos = this.advisors.size();
        addAdvisor(pos, advisor);
    }

    @Override
    public void addAdvisor(int pos, Advisor advisor) {
        assert pos <= this.advisors.size() : "Illegal position " + pos + " in advisor list with size " + this.advisors.size();
        this.advisors.add(pos, advisor);
        adviceChanged();
    }

    @Override
    public boolean removeAdvisor(Advisor advisor) {
        int index = indexOf(advisor);
        if (index == -1) {
            return false;
        }
        removeAdvisor(index);
        return true;
    }

    @Override
    public void removeAdvisor(int index) {
        assert index >= 0 && index <= this.advisors.size() - 1 : "Advisor index " + index + " is out of bounds: This configuration only has " + this.advisors.size() + " advisors.";
        this.advisors.remove(index);
        adviceChanged();
    }

    @Override
    public int indexOf(Advisor advisor) {
        return this.advisors.indexOf(advisor);
    }

    public void addAdvisors(Advisor... advisors) {
        addAdvisors(Arrays.asList(advisors));
    }

    public void addAdvisors(Collection<Advisor> advisors) {
        if (!CollectionUtils.isEmpty(advisors)) {
            for (Advisor advisor : advisors) {
                this.advisors.add(advisor);
            }
            adviceChanged();
        }
    }

    @Override
    public void addAdvice(Advice advice) {
        int pos = this.advisors.size();
        addAdvice(pos, advice);
    }

    @Override
    public void addAdvice(int pos, Advice advice) {
        addAdvisor(pos, new DefaultPointcutAdvisor(advice));
    }

    @Override
    public boolean removeAdvice(Advice advice) {
        int index = indexOf(advice);
        if (index == -1) {
            return false;
        }
        removeAdvisor(index);
        return true;
    }

    @Override
    public int indexOf(Advice advice) {
        for (int i = 0; i < this.advisors.size(); i++) {
            Advisor advisor = this.advisors.get(i);
            if (advisor.getAdvice() == advice) {
                return i;
            }
        }
        return -1;
    }

}
