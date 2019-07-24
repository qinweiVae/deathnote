package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.aop.support.AopUtils;
import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.beans.bean.BeanDefinitionHolder;
import com.qinwei.deathnote.beans.bean.RootBeanDefinition;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;

/**
 * @author qinwei
 * @date 2019-07-22
 */
public class ScopedProxyUtils {

    private static final String TARGET_NAME_PREFIX = "scopedTarget.";

    public static BeanDefinitionHolder createScopedProxy(BeanDefinitionHolder definition,
                                                         BeanDefinitionRegistry registry,
                                                         boolean proxyTargetClass) {

        String originalBeanName = definition.getBeanName();
        BeanDefinition originalDefinition = definition.getBeanDefinition();
        String targetBeanName = getTargetBeanName(originalBeanName);
        // 创建 代理 的 BeanDefinition
        RootBeanDefinition proxyDefinition = new RootBeanDefinition(ScopedProxyFactoryBean.class);
        proxyDefinition.setDecoratedDefinition(new BeanDefinitionHolder(originalDefinition, targetBeanName));
        proxyDefinition.setPrimary(originalDefinition.isPrimary());
        // 设置  targetBeanName 属性
        proxyDefinition.getPropertyValues().put("targetBeanName", targetBeanName);

        if (proxyTargetClass) {
            // 设置属性，用于判断是否需要进行cglib代理
            originalDefinition.setAttribute(AopUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
        } else {
            proxyDefinition.getPropertyValues().put("proxyTargetClass", Boolean.FALSE);
        }

        originalDefinition.setPrimary(false);
        //使用 targetBeanName 注册 原始的 BeanDefinition
        registry.registerBeanDefinition(targetBeanName, originalDefinition);

        return new BeanDefinitionHolder(proxyDefinition, originalBeanName, definition.getAliases());
    }

    public static String getTargetBeanName(String originalBeanName) {
        return TARGET_NAME_PREFIX + originalBeanName;
    }
}
