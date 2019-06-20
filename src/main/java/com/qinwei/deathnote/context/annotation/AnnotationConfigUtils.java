package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.beans.bean.AnnotatedBeanDefinition;
import com.qinwei.deathnote.beans.bean.BeanDefinitionHolder;
import com.qinwei.deathnote.beans.bean.RootBeanDefinition;
import com.qinwei.deathnote.beans.postprocessor.AutowiredAnnotationBeanPostProcessor;
import com.qinwei.deathnote.beans.postprocessor.ConfigurationClassPostProcessor;
import com.qinwei.deathnote.beans.postprocessor.EventListenerMethodProcessor;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.context.event.DefaultEventListenerFactory;
import com.qinwei.deathnote.context.metadata.AnnotatedTypeMetadata;
import com.qinwei.deathnote.context.metadata.ScopeMetadata;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-17
 */
public class AnnotationConfigUtils {


    private static final String CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME = "internalConfigurationAnnotationProcessor";

    private static final String AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME = "internalAutowiredAnnotationProcessor";

    private static final String EVENT_LISTENER_PROCESSOR_BEAN_NAME = "internalEventListenerProcessor";

    private static final String EVENT_LISTENER_FACTORY_BEAN_NAME = "internalEventListenerFactory";

    /**
     * 注册常用的 postprocessor
     */
    public static Set<BeanDefinitionHolder> registerAnnotationConfigProcessors(BeanDefinitionRegistry registry) {
        Set<BeanDefinitionHolder> beanHolders = new LinkedHashSet<>(8);
        // 处理 @Configuration 的 postprocessor
        if (!registry.containsBeanDefinition(CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)) {
            RootBeanDefinition bd = new RootBeanDefinition(ConfigurationClassPostProcessor.class);
            beanHolders.add(registerPostProcessor(registry, bd, CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME));
        }
        // 处理 @Autowired 和 @Value 的 postprocessor
        if (!registry.containsBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
            RootBeanDefinition bd = new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class);
            beanHolders.add(registerPostProcessor(registry, bd, AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
        }
        // 处理 @EventListener 的 postprocessor
        if (!registry.containsBeanDefinition(EVENT_LISTENER_PROCESSOR_BEAN_NAME)) {
            RootBeanDefinition bd = new RootBeanDefinition(EventListenerMethodProcessor.class);
            beanHolders.add(registerPostProcessor(registry, bd, EVENT_LISTENER_PROCESSOR_BEAN_NAME));
        }

        if (!registry.containsBeanDefinition(EVENT_LISTENER_FACTORY_BEAN_NAME)) {
            RootBeanDefinition bd = new RootBeanDefinition(DefaultEventListenerFactory.class);
            beanHolders.add(registerPostProcessor(registry, bd, EVENT_LISTENER_FACTORY_BEAN_NAME));
        }

        return beanHolders;
    }

    /**
     * 注册 PostProcessor
     */
    private static BeanDefinitionHolder registerPostProcessor(BeanDefinitionRegistry registry, RootBeanDefinition definition, String beanName) {
        registry.registerBeanDefinition(beanName, definition);
        return new BeanDefinitionHolder(definition, beanName);
    }

    /**
     * 处理常用的 bean 注解( @Lazy,@Primary,@DependsOn )
     */
    public static void processCommonDefinitionAnnotations(AnnotatedBeanDefinition abd) {
        processCommonDefinitionAnnotations(abd, abd.getMetadata());
    }

    /**
     * 处理常用的 bean 注解( @Lazy,@Primary,@DependsOn )
     */
    public static void processCommonDefinitionAnnotations(AnnotatedBeanDefinition abd, AnnotatedTypeMetadata metadata) {
        // 解析 @Lazy 注解
        AnnotationAttributes lazy = attributesFor(metadata, Lazy.class);
        if (lazy != null) {
            abd.setLazyInit(lazy.getBoolean("value"));
        } else if (abd.getMetadata() != metadata) {
            lazy = attributesFor(abd.getMetadata(), Lazy.class);
            if (lazy != null) {
                abd.setLazyInit(lazy.getBoolean("value"));
            }
        }
        // 解析 @Primary 注解
        if (metadata.isAnnotated(Primary.class.getName())) {
            abd.setPrimary(true);
        }
        //解析 @DependsOn 注解
        AnnotationAttributes dependsOn = attributesFor(metadata, DependsOn.class);
        if (dependsOn != null) {
            abd.setDependsOn(dependsOn.getStringArray("value"));
        }
    }

    /**
     * 获取 AnnotatedTypeMetadata 上 指定注解类型的所有属性
     */
    public static AnnotationAttributes attributesFor(AnnotatedTypeMetadata metadata, Class<?> annotationClass) {
        return attributesFor(metadata, annotationClass.getName());
    }

    /**
     * 获取 AnnotatedTypeMetadata 上 指定注解名称的所有属性
     */
    public static AnnotationAttributes attributesFor(AnnotatedTypeMetadata metadata, String annotationClassName) {
        return AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(annotationClassName));
    }

    /**
     * 根据不同的 scope 模式 创建不同的 BeanDefinition
     */
    public static BeanDefinitionHolder applyScopedProxyMode(ScopeMetadata metadata, BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
        ScopedProxyMode scopedProxyMode = metadata.getScopedProxyMode();
        if (scopedProxyMode.equals(ScopedProxyMode.NO)) {
            return definitionHolder;
        }
        boolean proxyTargetClass = scopedProxyMode.equals(ScopedProxyMode.TARGET_CLASS);
        //todo 如果是代理类，需要特殊处理
        return null;
    }
}
