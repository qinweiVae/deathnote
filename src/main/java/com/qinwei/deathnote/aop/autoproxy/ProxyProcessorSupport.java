package com.qinwei.deathnote.aop.autoproxy;

import com.qinwei.deathnote.aop.aspectj.Advice;
import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.aspectj.Pointcut;
import com.qinwei.deathnote.utils.ClassUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qinwei
 * @date 2019-06-27
 */
@Slf4j
public class ProxyProcessorSupport implements AopInfrastructureBean {

    private ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();

    public ClassLoader getProxyClassLoader() {
        return proxyClassLoader;
    }

    public void setProxyClassLoader(ClassLoader proxyClassLoader) {
        this.proxyClassLoader = proxyClassLoader;
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
