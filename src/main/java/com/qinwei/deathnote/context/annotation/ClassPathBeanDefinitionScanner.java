package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.beans.bean.AnnotatedBeanDefinition;
import com.qinwei.deathnote.beans.bean.AnnotatedGenericBeanDefinition;
import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.beans.bean.BeanDefinitionHolder;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.context.support.AnnotationBeanNameGenerator;
import com.qinwei.deathnote.context.support.BeanDefinitionReaderUtils;
import com.qinwei.deathnote.context.support.BeanNameGenerator;
import com.qinwei.deathnote.context.metadata.ScopeMetadata;
import com.qinwei.deathnote.support.scan.TypeAnnotationScanner;
import com.qinwei.deathnote.utils.AnnotationUtils;
import com.qinwei.deathnote.utils.ObjectUtils;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-13
 * <p>
 * 用于扫描 @Component 注解
 */
public class ClassPathBeanDefinitionScanner {

    private final BeanDefinitionRegistry registry;

    private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    private TypeAnnotationScanner scanner = new TypeAnnotationScanner();

    private final List<Class<? extends Annotation>> includeFilters = new LinkedList<>();

    private final List<Class<? extends Annotation>> excludeFilters = new LinkedList<>();

    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        this(registry, true);
    }


    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        this.registry = registry;
        if (useDefaultFilters) {
            registerDefaultFilters();
        }
    }

    public final BeanDefinitionRegistry getRegistry() {
        return this.registry;
    }

    public int scan(String... basePackages) {
        int beanCountAtScanStart = this.registry.getBeanDefinitionCount();

        doScan(basePackages);
        //注册常用的 postprocessor
        AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);

        return (this.registry.getBeanDefinitionCount() - beanCountAtScanStart);
    }

    /**
     * 扫描指定包下的 BeanDefinition
     */
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        if (ObjectUtils.isEmpty(basePackages)) {
            throw new IllegalArgumentException("At least one base package must be specified");
        }
        Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();
        for (String basePackage : basePackages) {
            //找到所有 符合条件的 BeanDefinition
            Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
            for (BeanDefinition candidate : candidates) {
                ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);
                candidate.setScope(scopeMetadata.getScopeName());
                String beanName = this.beanNameGenerator.generateBeanName(candidate, this.registry);
                if (candidate instanceof AnnotatedBeanDefinition) {
                    //处理常用的 bean 注解( @Lazy,@Primary,@DependsOn )
                    AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) candidate);
                }
                // 判断 BeanDefinition 是否已经 注册过
                if (checkCandidate(beanName, candidate)) {
                    BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
                    //根据不同的 scope 模式 创建不同的 BeanDefinition
                    definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
                    beanDefinitions.add(definitionHolder);
                    BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
                }
            }
        }
        return beanDefinitions;
    }

    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) {
        if (!this.registry.containsBeanDefinition(beanName)) {
            return true;
        }
        return false;
    }

    /**
     * 找到所有 符合条件的 BeanDefinition
     */
    protected Set<BeanDefinition> findCandidateComponents(String basePackage) {
        Set<BeanDefinition> candidates = new LinkedHashSet<>();
        Set<Class> classes = scanner.scan(basePackage);
        for (Class clazz : classes) {
            if (isCandidateComponent(clazz)) {
                AnnotatedBeanDefinition abd = new AnnotatedGenericBeanDefinition(clazz);
                candidates.add(abd);
            }
        }
        return candidates;
    }

    /**
     * 判断 class 上 是否含有 指定的 注解
     */
    protected boolean isCandidateComponent(Class clazz) {
        for (Class<? extends Annotation> filter : excludeFilters) {
            if (AnnotationUtils.findAnnotation(clazz, filter) != null) {
                return false;
            }
        }
        for (Class<? extends Annotation> filter : includeFilters) {
            if (AnnotationUtils.findAnnotation(clazz, filter) != null) {
                return true;
            }
        }
        return false;
    }

    protected void registerDefaultFilters() {
        this.includeFilters.add(Component.class);
    }

    public void addIncludeFilter(Class<? extends Annotation> annotation) {
        this.includeFilters.add(annotation);
    }

    public void addExcludeFilter(Class<? extends Annotation> annotation) {
        this.excludeFilters.add(annotation);
    }

    public void setScopedProxyMode(ScopedProxyMode scopedProxyMode) {
        this.scopeMetadataResolver = new AnnotationScopeMetadataResolver(scopedProxyMode);
    }
}

