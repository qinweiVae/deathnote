package com.qinwei.deathnote.context.aop;

import com.qinwei.deathnote.context.annotation.Bean;
import com.qinwei.deathnote.context.annotation.Configuration;
import com.qinwei.deathnote.context.annotation.EnableAspectJAutoProxy;
import com.qinwei.deathnote.context.annotation.Scope;
import com.qinwei.deathnote.context.annotation.ScopedProxyMode;

/**
 * @author qinwei
 * @date 2019-07-23
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = false, exposeProxy = true)
public class AutoProxyTest {

    /**
     * 如果@EnableAspectJAutoProxy 注解的 proxyTargetClass = false ，Man 使用 jdk 动态代理，
     * proxyTargetClass = true ，Man 使用 CGLib 代理；
     * 如果利用 aspectj 拦截了 Man 中的方法， 不能再设置 @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
     */
    @Bean
    @Scope(proxyMode = ScopedProxyMode.INTERFACES)
    //@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Man man() {
        return new Man();
    }

    @Bean
    public FactoryBeanTest factoryBeanTest() {
        return new FactoryBeanTest();
    }
}
