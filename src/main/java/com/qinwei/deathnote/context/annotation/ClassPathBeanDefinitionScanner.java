package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

/**
 * @author qinwei
 * @date 2019-06-13
 * <p>
 * 用于扫描 @Component 注解
 */
public class ClassPathBeanDefinitionScanner {

    private final BeanDefinitionRegistry registry;

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

    protected void registerDefaultFilters() {
        this.includeFilters.add(Component.class);
    }

    public void addIncludeFilter(Class<? extends Annotation> annotation) {
        this.includeFilters.add(annotation);
    }

    public void addExcludeFilter(Class<? extends Annotation> annotation) {
        this.excludeFilters.add(annotation);
    }
}

