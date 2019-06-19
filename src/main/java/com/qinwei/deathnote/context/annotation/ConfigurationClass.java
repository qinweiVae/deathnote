package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.context.metadata.AnnotationMetadata;
import com.qinwei.deathnote.context.metadata.StandardAnnotationMetadata;

/**
 * @author qinwei
 * @date 2019-06-19
 */
public class ConfigurationClass {

    private final AnnotationMetadata metadata;

    private String beanName;

    public ConfigurationClass(AnnotationMetadata metadata, String beanName) {
        this.metadata = metadata;
        this.beanName = beanName;
    }

    public ConfigurationClass(Class<?> clazz, String beanName) {
        this.metadata = new StandardAnnotationMetadata(clazz, true);
        this.beanName = beanName;
    }

    public AnnotationMetadata getMetadata() {
        return metadata;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
