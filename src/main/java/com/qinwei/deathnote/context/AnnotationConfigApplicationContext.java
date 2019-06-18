package com.qinwei.deathnote.context;

import com.qinwei.deathnote.context.annotation.AnnotatedBeanDefinitionReader;
import com.qinwei.deathnote.context.annotation.AnnotationConfigRegistry;
import com.qinwei.deathnote.context.annotation.ClassPathBeanDefinitionScanner;
import com.qinwei.deathnote.utils.ObjectUtils;

/**
 * @author qinwei
 * @date 2019-06-13
 */
public class AnnotationConfigApplicationContext extends GenericApplicationContext implements AnnotationConfigRegistry {

    private final AnnotatedBeanDefinitionReader reader;

    private final ClassPathBeanDefinitionScanner scanner;

    public AnnotationConfigApplicationContext() {
        this.reader = new AnnotatedBeanDefinitionReader(this);
        this.scanner = new ClassPathBeanDefinitionScanner(this);
    }

    public AnnotationConfigApplicationContext(Class<?>... annotatedClasses) {
        this();
        register(annotatedClasses);
        refresh();
    }

    public AnnotationConfigApplicationContext(String... basePackages) {
        this();
        scan(basePackages);
        refresh();
    }

    @Override
    public void register(Class<?>... annotatedClass) {
        if (ObjectUtils.isEmpty(annotatedClass)) {
            throw new IllegalArgumentException("At least one annotated class must be specified");
        }
        this.reader.register(annotatedClass);
    }

    @Override
    public void scan(String... basePackages) {
        if (ObjectUtils.isEmpty(basePackages)) {
            throw new IllegalArgumentException("At least one base package class must be specified");
        }
        this.scanner.scan(basePackages);
    }
}
