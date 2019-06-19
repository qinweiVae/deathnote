package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.beans.bean.BeanDefinitionHolder;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.CollectionUtils;
import com.qinwei.deathnote.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-19
 */
public class ComponentScanAnnotationParser {

    private final BeanDefinitionRegistry registry;

    public ComponentScanAnnotationParser(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    /**
     * 解析 @ComponentScan 注解
     */
    public Set<BeanDefinitionHolder> parse(AnnotationAttributes componentScan, String declaringClass) {
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
}
