package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.aop.AopInfrastructureBean;
import com.qinwei.deathnote.aop.ProxyConfig;
import com.qinwei.deathnote.aop.autoproxy.ProxyFactory;
import com.qinwei.deathnote.aop.targetSource.SimpleBeanTargetSource;
import com.qinwei.deathnote.beans.bean.FactoryBean;
import com.qinwei.deathnote.beans.factory.BeanFactory;
import com.qinwei.deathnote.beans.factory.ConfigurableBeanFactory;
import com.qinwei.deathnote.context.aware.BeanFactoryAware;
import com.qinwei.deathnote.utils.ClassUtils;

import java.lang.reflect.Modifier;

/**
 * @author qinwei
 * @date 2019-07-23
 */
public class ScopedProxyFactoryBean extends ProxyConfig implements FactoryBean<Object>, BeanFactoryAware, AopInfrastructureBean {

    private String targetBeanName;

    private final SimpleBeanTargetSource scopedTargetSource = new SimpleBeanTargetSource();

    private Object proxy;

    public ScopedProxyFactoryBean() {
        setProxyTargetClass(true);
    }

    public void setTargetBeanName(String targetBeanName) {
        this.targetBeanName = targetBeanName;
        this.scopedTargetSource.setTargetBeanName(targetBeanName);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof ConfigurableBeanFactory)) {
            throw new IllegalStateException("Not running in a ConfigurableBeanFactory: " + beanFactory);
        }
        ConfigurableBeanFactory cbf = (ConfigurableBeanFactory) beanFactory;

        this.scopedTargetSource.setBeanFactory(beanFactory);

        ProxyFactory pf = new ProxyFactory();
        pf.copyFrom(this);
        pf.setTargetSource(this.scopedTargetSource);

        if (this.targetBeanName == null) {
            throw new IllegalArgumentException("targetBeanName is null");
        }
        Class<?> beanType = beanFactory.getType(this.targetBeanName);
        if (beanType == null) {
            throw new IllegalStateException("Cannot create scoped proxy for bean '" + this.targetBeanName +
                    "': Target type could not be determined at the time of proxy creation.");
        }
        if (!isProxyTargetClass() || beanType.isInterface() || Modifier.isPrivate(beanType.getModifiers())) {
            pf.setInterfaces(ClassUtils.getAllInterfacesAsSet(beanType).toArray(new Class<?>[0]));
        }
        // 添加 AopInfrastructureBean 标识 代理类 不会被 自动代理
        pf.addInterface(AopInfrastructureBean.class);

        this.proxy = pf.getProxy(cbf.getBeanClassLoader());
    }

    @Override
    public Object getObject() throws Exception {
        if (this.proxy == null) {
            throw new IllegalStateException("the proxy can not be null , bean name : " + this.targetBeanName);
        }
        return this.proxy;
    }

    @Override
    public Class<?> getObjectType() {
        if (this.proxy != null) {
            return this.proxy.getClass();
        }
        return this.scopedTargetSource.getTargetClass();
    }
}
