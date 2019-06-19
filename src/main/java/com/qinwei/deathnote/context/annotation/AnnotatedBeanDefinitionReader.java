package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.beans.bean.AnnotatedGenericBeanDefinition;
import com.qinwei.deathnote.beans.bean.BeanDefinitionHolder;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.context.support.AnnotationBeanNameGenerator;
import com.qinwei.deathnote.context.support.BeanDefinitionReaderUtils;
import com.qinwei.deathnote.context.support.BeanNameGenerator;
import com.qinwei.deathnote.context.metadata.ScopeMetadata;

/**
 * @author qinwei
 * @date 2019-06-17
 */
public class AnnotatedBeanDefinitionReader {

    private final BeanDefinitionRegistry registry;

    private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();


    public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
        this.registry = registry;
        AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
    }

    public final BeanDefinitionRegistry getRegistry() {
        return this.registry;
    }

    public void register(Class<?>... annotatedClasses) {
        for (Class<?> annotatedClass : annotatedClasses) {
            registerBean(annotatedClass);
        }
    }

    public void registerBean(Class<?> annotatedClass) {
        doRegisterBean(annotatedClass);
    }

    /**
     * 注册 bean
     */
    <T> void doRegisterBean(Class<T> annotatedClass) {

        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(annotatedClass);
        //解析 @scope 注解
        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
        abd.setScope(scopeMetadata.getScopeName());
        //生成 beanName
        String beanName = this.beanNameGenerator.generateBeanName(abd, this.registry);
        //处理常用的 bean 注解( @Lazy,@Primary,@DependsOn )
        AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);

        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
        //根据不同的 scope 模式 创建不同的 BeanDefinition
        definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
        //注册 BeanDefinition
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
    }

}
