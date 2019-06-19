package com.qinwei.deathnote.context.support;

import com.qinwei.deathnote.beans.bean.BeanDefinitionHolder;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;

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
    }
}
