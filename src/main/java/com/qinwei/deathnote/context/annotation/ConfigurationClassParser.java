package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.beans.bean.AbstractBeanDefinition;
import com.qinwei.deathnote.beans.bean.AnnotatedBeanDefinition;
import com.qinwei.deathnote.beans.bean.AnnotatedGenericBeanDefinition;
import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.beans.bean.BeanDefinitionHolder;
import com.qinwei.deathnote.beans.factory.AutowireCapableBeanFactory;
import com.qinwei.deathnote.beans.factory.BeanFactory;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.context.aware.BeanFactoryAware;
import com.qinwei.deathnote.context.metadata.AnnotationMetadata;
import com.qinwei.deathnote.context.metadata.MethodMetadata;
import com.qinwei.deathnote.context.support.ConfigurationUtils;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.CollectionUtils;
import com.qinwei.deathnote.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-19
 */
public class ConfigurationClassParser {

    private final BeanDefinitionRegistry registry;

    public ConfigurationClassParser(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    /**
     * 处理 @ComponentScan,@Import,@Bean 注解
     */
    public void parse(Set<BeanDefinitionHolder> configCandidates) {
        for (BeanDefinitionHolder holder : configCandidates) {
            BeanDefinition bd = holder.getBeanDefinition();
            if (bd instanceof AnnotatedBeanDefinition) {
                parse(((AnnotatedBeanDefinition) bd).getMetadata(), holder.getBeanName());
            } else if (bd instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) bd).hasBeanClass()) {
                parse(((AbstractBeanDefinition) bd).getBeanClass(), holder.getBeanName());
            }
        }
    }

    private void parse(AnnotationMetadata metadata, String beanName) {
        processConfigurationClass(new ConfigurationClass(metadata, beanName));
    }

    private void parse(Class clazz, String beanName) {
        processConfigurationClass(new ConfigurationClass(clazz, beanName));
    }

    /**
     * 解析@ComponentScan,@Import,@Bean 注解
     */
    protected void processConfigurationClass(ConfigurationClass configurationClass) {
        // 处理 @ComponentScan 注解
        processComponentScans(configurationClass);
        //处理 @Import 注解
        processImports(configurationClass.getMetadata());
        //处理 @Bean 注解
        processBeans(configurationClass);
    }

    /**
     * 处理 @ComponentScan 注解
     */
    private void processComponentScans(ConfigurationClass configurationClass) {
        AnnotationAttributes componentScan = AnnotationConfigUtils.attributesFor(configurationClass.getMetadata(), ComponentScan.class.getName());
        //解析 @ComponentScan 注解， 扫描指定包下的 BeanDefinition
        Set<BeanDefinitionHolder> beanDefinitionHolders = parse(componentScan, configurationClass.getMetadata().getClassName());
        for (BeanDefinitionHolder holder : beanDefinitionHolders) {
            BeanDefinition bd = holder.getBeanDefinition();
            // 检查 bean 上是否有 @Configuration,@ComponentScan,@Import 注解，有则解析 @Configuration 注解
            if (ConfigurationUtils.checkConfigurationClassCandidate(bd)) {
                parse(bd.getBeanClass(), holder.getBeanName());
            }
        }
    }

    /**
     * 解析 @ComponentScan 注解， 扫描指定包下的 BeanDefinition
     */
    private Set<BeanDefinitionHolder> parse(AnnotationAttributes componentScan, String declaringClass) {
        if (CollectionUtils.isEmpty(componentScan)) {
            return new HashSet<>();
        }
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(this.registry, componentScan.getBoolean("useDefaultFilters"));
        ScopedProxyMode scopedProxyMode = componentScan.getEnum("scopedProxy");
        if (scopedProxyMode != ScopedProxyMode.DEFAULT) {
            scanner.setScopedProxyMode(scopedProxyMode);
        }
        Class<?>[] includeFilters = componentScan.getClassArray("includeFilters");
        for (Class<?> filter : includeFilters) {
            scanner.addIncludeFilter((Class<? extends Annotation>) filter);
        }
        Class<?>[] excludeFilters = componentScan.getClassArray("excludeFilters");
        for (Class<?> filter : excludeFilters) {
            scanner.addExcludeFilter((Class<? extends Annotation>) filter);
        }
        // 需要扫描的包路径
        Set<String> basePackages = new LinkedHashSet<>();
        String[] basePackagesArray = componentScan.getStringArray("basePackages");
        Collections.addAll(basePackages, basePackagesArray);
        // 如果配的是 class ，扫描class 所在的包路径
        Class<?>[] basePackageClasses = componentScan.getClassArray("basePackageClasses");
        for (Class<?> packageClass : basePackageClasses) {
            basePackages.add(ClassUtils.getPackageName(packageClass));
        }
        //如果没有指定包路径或者class，则取当前 bean 所在的包路径
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(declaringClass));
        }
        //扫描指定包下的 BeanDefinition
        return scanner.doScan(StringUtils.toArray(basePackages));
    }

    /**
     * 处理 @Bean 注解
     */
    private void processBeans(ConfigurationClass configurationClass) {
        Set<MethodMetadata> beanMethods = configurationClass.getMetadata().getAnnotatedMethods(Bean.class.getName());
        for (MethodMetadata methodMetadata : beanMethods) {
            // @Bean 注解不能放在 抽象方法、final方法、private方法、static方法 上
            if (methodMetadata.isAbstract() || !methodMetadata.isOverridable()) {
                throw new RuntimeException("Unable to parse @Bean on method which is static or abstract or final or unOverridable");
            }
            // @Bean注解所在方法的方法名
            String methodName = methodMetadata.getMethodName();
            // @Bean注解 的所有属性
            AnnotationAttributes bean = AnnotationConfigUtils.attributesFor(methodMetadata, Bean.class);
            List<String> names = new ArrayList<>(Arrays.asList(bean.getStringArray("value")));
            // 如果没有指定 beanName 则使用 方法名；指定了 beanName 则使用第一个name 作为 beanName，其余的name作为别名
            String beanName = CollectionUtils.isEmpty(names) ? methodName : names.remove(0);
            for (String name : names) {
                this.registry.registerAlias(beanName, name);
            }
            // 创建 BeanDefinition
            String returnTypeName = methodMetadata.getReturnTypeName();
            AnnotatedGenericBeanDefinition bd = new AnnotatedGenericBeanDefinition(ClassUtils.forName(returnTypeName));
            bd.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR);
            //设置 添加了 @Bean 注解的 方法
            bd.setFactoryMethod(methodMetadata.getIntrospectedMethod());
            //设置 添加了 @Bean 注解 所在类的 bean name
            bd.setFactoryBeanName(configurationClass.getBeanName());
            //处理常用的 bean 注解( @Lazy,@Primary,@DependsOn )
            AnnotationConfigUtils.processCommonDefinitionAnnotations(bd, methodMetadata);
            //解析 initMethod 和 destroyMethod
            String initMethod = bean.getString("initMethod");
            if (StringUtils.isNotEmpty(initMethod)) {
                bd.setInitMethodName(initMethod);
            }
            String destroyMethod = bean.getString("destroyMethod");
            if (StringUtils.isNotEmpty(destroyMethod)) {
                bd.setDestroyMethodName(destroyMethod);
            }
            // 如果有 @Scope 注解
            ScopedProxyMode proxyMode = ScopedProxyMode.NO;
            AnnotationAttributes scope = AnnotationConfigUtils.attributesFor(methodMetadata, Scope.class);
            if (CollectionUtils.isNotEmpty(scope)) {
                bd.setScope(scope.getString("value"));
                proxyMode = scope.getEnum("proxyMode");
                if (proxyMode == ScopedProxyMode.DEFAULT) {
                    proxyMode = ScopedProxyMode.NO;
                }
            }
            BeanDefinition bdToRegister = bd;
            if (proxyMode != ScopedProxyMode.NO) {
                //todo 如果 bean 需要 代理
            }
            //注册 BeanDefinition
            if (!registry.containsBeanDefinition(beanName)) {
                this.registry.registerBeanDefinition(beanName, bdToRegister);
            }
        }
    }

    /**
     * 处理 @Import 注解
     */
    private void processImports(AnnotationMetadata metadata) {
        // 获取 @Import 注解 的所有属性
        AnnotationAttributes imports = AnnotationConfigUtils.attributesFor(metadata, Import.class.getName());
        if (CollectionUtils.isEmpty(imports)) {
            return;
        }
        Class<?>[] values = imports.getClassArray("value");
        for (Class<?> value : values) {
            // 如果是 ImportSelector
            if (ClassUtils.isAssignable(ImportSelector.class, value)) {
                //加载class
                Class<?> clazz = ClassUtils.forName(value.getName());
                // 实例化 ImportSelector
                ImportSelector selector = ClassUtils.instantiateClass(clazz, ImportSelector.class);
                // 如果 selector 实现了 Aware
                if (selector instanceof BeanFactoryAware && this.registry instanceof BeanFactory) {
                    ((BeanFactoryAware) selector).setBeanFactory((BeanFactory) this.registry);
                }
                String[] importClassNames = selector.selectImports(metadata);
                for (String importClassName : importClassNames) {
                    Class<?> importClass = ClassUtils.forName(importClassName);
                    // 递归处理
                    processConfigurationClass(new ConfigurationClass(importClass));
                }
            }
            // 如果是 ImportBeanDefinitionRegistrar
            else if (ClassUtils.isAssignable(ImportBeanDefinitionRegistrar.class, value)) {
                //加载class
                Class<?> clazz = ClassUtils.forName(value.getName());
                // 实例化 ImportBeanDefinitionRegistrar
                ImportBeanDefinitionRegistrar registrar = ClassUtils.instantiateClass(clazz, ImportBeanDefinitionRegistrar.class);
                // 如果 selector 实现了 Aware
                if (registrar instanceof BeanFactoryAware && this.registry instanceof BeanFactory) {
                    ((BeanFactoryAware) registrar).setBeanFactory((BeanFactory) this.registry);
                }
                //注册 BeanDefinition
                registrar.registerBeanDefinitions(metadata, this.registry);
            }
            //如果都不是，那就当成包含 @Configuration 的class 处理
            else {
                processConfigurationClass(new ConfigurationClass(value));
            }
        }
    }

}
