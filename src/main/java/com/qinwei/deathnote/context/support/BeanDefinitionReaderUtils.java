package com.qinwei.deathnote.context.support;

import com.qinwei.deathnote.beans.bean.BeanDefinitionHolder;
import com.qinwei.deathnote.beans.bean.FactoryBean;
import com.qinwei.deathnote.beans.factory.BeanFactory;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.context.annotation.ScopedProxyFactoryBean;
import com.qinwei.deathnote.utils.ClassUtils;

/**
 * @author qinwei
 * @date 2019-06-17
 */
public class BeanDefinitionReaderUtils {


    public static void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
        // 注册BeanDefinition
        String beanName = definitionHolder.getBeanName();
        registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());
        // 注册别名
        String[] aliases = definitionHolder.getAliases();
        if (aliases != null) {
            for (String alias : aliases) {
                registry.registerAlias(beanName, alias);
            }
        }
        // 如果是 FactoryBean 注册 &beanName 为其别名
        Class<?> beanClass = definitionHolder.getBeanDefinition().getBeanClass();
        if (ClassUtils.isAssignable(FactoryBean.class, beanClass) && beanClass != ScopedProxyFactoryBean.class) {
            registry.registerAlias(beanName, BeanFactory.FACTORY_BEAN_NAME + beanName);
        }
    }
}
