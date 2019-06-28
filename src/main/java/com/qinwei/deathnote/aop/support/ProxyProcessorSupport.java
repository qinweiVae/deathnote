package com.qinwei.deathnote.aop.support;

import com.qinwei.deathnote.aop.AopInfrastructureBean;
import com.qinwei.deathnote.aop.ProxyConfig;
import com.qinwei.deathnote.aop.aspectj.Advice;
import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.aspectj.Pointcut;
import com.qinwei.deathnote.aop.autoproxy.ProxyFactory;
import com.qinwei.deathnote.beans.bean.DisposableBean;
import com.qinwei.deathnote.beans.bean.InitializingBean;
import com.qinwei.deathnote.context.aware.Aware;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-27
 */
@Slf4j
public class ProxyProcessorSupport extends ProxyConfig implements AopInfrastructureBean {

    private ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();

    public ClassLoader getProxyClassLoader() {
        return proxyClassLoader;
    }

    public void setProxyClassLoader(ClassLoader proxyClassLoader) {
        this.proxyClassLoader = proxyClassLoader;
    }

    /**
     * 检测 beanClass 是否实现了接口，若未实现，则将 proxyFactory 的 proxyTargetClass 设为 true
     */
    protected void evaluateProxyInterfaces(Class<?> beanClass, ProxyFactory proxyFactory) {
        Set<Class<?>> interfaces = ClassUtils.getAllInterfacesAsSet(beanClass);
        boolean hasProxyInterface = interfaces.stream()
                .anyMatch(inf -> !isConfigurationCallbackInterface(inf) &&
                        !isInternalLanguageInterface(inf) &&
                        inf.getMethods().length > 0);

        if (hasProxyInterface) {
            for (Class<?> inf : interfaces) {
                proxyFactory.addInterface(inf);
            }
        } else {
            proxyFactory.setProxyTargetClass(true);
        }
    }

    protected boolean isConfigurationCallbackInterface(Class<?> ifc) {
        return (InitializingBean.class == ifc ||
                DisposableBean.class == ifc ||
                Closeable.class == ifc ||
                AutoCloseable.class == ifc ||
                ObjectUtils.containsElement(ifc.getInterfaces(), Aware.class));
    }

    protected boolean isInternalLanguageInterface(Class<?> ifc) {
        return ifc.getName().endsWith(".cglib.proxy.Factory") ||
                ifc.getName().endsWith(".bytebuddy.MockAccess");
    }

    /*
     * 如果是基础设施类（Pointcut、Advice、Advisor 等接口的实现类），或是应该跳过的类，
     * 则不应该生成代理，此时直接返回 bean
     */
    protected boolean isInfrastructureClass(Class<?> beanClass) {
        boolean isInfrastructure = ClassUtils.isAssignable(Advice.class, beanClass) ||
                ClassUtils.isAssignable(Pointcut.class, beanClass) ||
                ClassUtils.isAssignable(Advisor.class, beanClass) ||
                ClassUtils.isAssignable(AopInfrastructureBean.class, beanClass);

        if (isInfrastructure) {
            log.info("Did not attempt to auto-proxy infrastructure class [{}]", beanClass.getName());
        }
        return isInfrastructure;
    }
}
