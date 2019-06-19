package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.beans.bean.AbstractBeanDefinition;
import com.qinwei.deathnote.beans.bean.AnnotatedBeanDefinition;
import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.beans.bean.BeanDefinitionHolder;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.context.metadata.AnnotationMetadata;
import com.qinwei.deathnote.context.support.ConfigurationUtils;

import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-19
 */
public class ConfigurationClassParser {

    private final BeanDefinitionRegistry registry;

    private final ComponentScanAnnotationParser componentScanParser;


    public ConfigurationClassParser(BeanDefinitionRegistry registry) {
        this.registry = registry;
        this.componentScanParser = new ComponentScanAnnotationParser(registry);
    }

    /**
     * 解析 @Configuration,@ComponentScan,@Import 注解
     */
    public void parse(Set<BeanDefinitionHolder> configCandidates) {
        for (BeanDefinitionHolder holder : configCandidates) {
            BeanDefinition bd = holder.getBeanDefinition();
            if (bd instanceof AnnotatedBeanDefinition) {
                parse(((AnnotatedBeanDefinition) bd).getMetadata(), holder.getBeanName());
            } else if (bd instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) bd).hasBeanClass()) {
                parse(((AbstractBeanDefinition) bd).getBeanClass(), holder.getBeanName());
            }
            //todo
        }
    }

    private void parse(AnnotationMetadata metadata, String beanName) {
        processConfigurationClass(new ConfigurationClass(metadata, beanName));
    }

    private void parse(Class clazz, String beanName) {
        processConfigurationClass(new ConfigurationClass(clazz, beanName));
    }

    /**
     * 解析 @Configuration,@ComponentScan,@Import 注解
     */
    protected void processConfigurationClass(ConfigurationClass configurationClass) {
        // 解析 @ComponentScan 注解
        AnnotationAttributes componentScan = AnnotationConfigUtils.attributesFor(configurationClass.getMetadata(), ComponentScan.class.getName());
        Set<BeanDefinitionHolder> beanDefinitionHolders = this.componentScanParser.parse(componentScan, configurationClass.getMetadata().getClassName());
        for (BeanDefinitionHolder holder : beanDefinitionHolders) {
            BeanDefinition bd = holder.getBeanDefinition();
            // 检查 bean 上是否有 @Configuration,@ComponentScan,@Import 注解，有则解析 @Configuration 注解
            if (ConfigurationUtils.checkConfigurationClassCandidate(bd)) {
                parse(bd.getBeanClass(), holder.getBeanName());
            }
        }
        //todo 解析 @Import 注解

    }

}
