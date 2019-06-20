package com.qinwei.deathnote.beans.postprocessor;

import com.qinwei.deathnote.beans.bean.AbstractBeanDefinition;
import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.beans.bean.BeanDefinitionHolder;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.context.annotation.AnnotationOrderComparator;
import com.qinwei.deathnote.context.annotation.ConfigurationClassParser;
import com.qinwei.deathnote.context.support.ConfigurationUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-18
 */
@Slf4j
public class ConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private final Set<Integer> registriesPostProcessed = new HashSet<>();

    private final Set<Integer> factoriesPostProcessed = new HashSet<>();

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        int registryId = System.identityHashCode(registry);
        if (this.registriesPostProcessed.contains(registryId)) {
            throw new IllegalStateException(
                    "postProcessBeanDefinitionRegistry already called on this post-processor against " + registry);
        }
        if (this.factoriesPostProcessed.contains(registryId)) {
            throw new IllegalStateException(
                    "postProcessBeanFactory already called on this post-processor against " + registry);
        }
        this.registriesPostProcessed.add(registryId);
        //处理 @Configuration,@ComponentScan,@Import ,@Bean 注解
        processConfigBeanDefinitions(registry);

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        int factoryId = System.identityHashCode(beanFactory);
        if (this.factoriesPostProcessed.contains(factoryId)) {
            throw new IllegalStateException(
                    "postProcessBeanFactory already called on this post-processor against " + beanFactory);
        }
        this.factoriesPostProcessed.add(factoryId);
        if (!this.registriesPostProcessed.contains(factoryId)) {
            // ConfigurableListableBeanFactory 只有 DefaultListableBeanFactory 一个实现类，可以强转成BeanDefinitionRegistry
            processConfigBeanDefinitions((BeanDefinitionRegistry) beanFactory);
        }
    }

    /**
     * 处理 @Configuration,@ComponentScan,@Import ,@Bean 注解
     */
    public void processConfigBeanDefinitions(BeanDefinitionRegistry registry) {
        List<BeanDefinitionHolder> configCandidates = new ArrayList<>();
        String[] candidateNames = registry.getBeanDefinitionNames();
        for (String beanName : candidateNames) {
            BeanDefinition bd = registry.getBeanDefinition(beanName);
            //检查 bean 上是否有 @Configuration,@ComponentScan,@Import 注解
            if (ConfigurationUtils.checkConfigurationClassCandidate(bd)) {
                configCandidates.add(new BeanDefinitionHolder(bd, beanName));
            }
        }
        // 如果没有class 上包含 @Configuration,@ComponentScan,@Import 注解
        if (configCandidates.isEmpty()) {
            return;
        }
        // 按照 @Order 注解排序
        configCandidates.sort((o1, o2) -> AnnotationOrderComparator.INSTANCE.compare(((AbstractBeanDefinition) o1.getBeanDefinition()).getBeanClass(), ((AbstractBeanDefinition) o2.getBeanDefinition()).getBeanClass()));

        Set<BeanDefinitionHolder> candidates = new LinkedHashSet<>(configCandidates);
        // 创建 @Configuration  解析器
        ConfigurationClassParser parser = new ConfigurationClassParser(registry);
        // 处理 @ComponentScan,@Import,@Bean 注解
        parser.parse(candidates);
    }

}
