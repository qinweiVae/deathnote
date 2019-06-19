package com.qinwei.deathnote.context.support;

import com.qinwei.deathnote.beans.bean.AbstractBeanDefinition;
import com.qinwei.deathnote.beans.bean.AnnotatedBeanDefinition;
import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.context.annotation.Bean;
import com.qinwei.deathnote.context.annotation.ComponentScan;
import com.qinwei.deathnote.context.annotation.Configuration;
import com.qinwei.deathnote.context.annotation.Import;
import com.qinwei.deathnote.context.metadata.AnnotationMetadata;
import com.qinwei.deathnote.context.metadata.StandardAnnotationMetadata;
import com.qinwei.deathnote.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-19
 */
@Slf4j
public class ConfigurationUtils {

    private static final Set<String> candidateIndicators = new HashSet<>(4);

    static {
        candidateIndicators.add(ComponentScan.class.getName());
        candidateIndicators.add(Import.class.getName());
    }

    public static boolean isConfigurationCandidate(AnnotationMetadata metadata) {
        if (metadata.isAnnotated(Configuration.class.getName())) {
            return true;
        }
        if (metadata.isInterface()) {
            return false;
        }
        for (String indicator : candidateIndicators) {
            if (metadata.isAnnotated(indicator)) {
                return true;
            }
        }
        try {
            return metadata.hasAnnotatedMethods(Bean.class.getName());
        } catch (Throwable ex) {
            log.warn("Failed to introspect @Bean methods on class [" + metadata.getClassName() + "]: " + ex);
        }
        return false;
    }

    /**
     * 检查 bean 上是否有 @Configuration,@ComponentScan,@Import 注解
     */
    public static boolean checkConfigurationClassCandidate(BeanDefinition bd) {
        String className = bd.getBeanClassName();
        if (StringUtils.isEmpty(className)) {
            return false;
        }
        AnnotationMetadata metadata;
        if (bd instanceof AnnotatedBeanDefinition &&
                className.equals(((AnnotatedBeanDefinition) bd).getMetadata().getClassName())) {
            metadata = ((AnnotatedBeanDefinition) bd).getMetadata();

        } else if (bd instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) bd).hasBeanClass()) {
            Class<?> beanClass = bd.getBeanClass();
            metadata = new StandardAnnotationMetadata(beanClass, true);

        } else {
            return false;
        }
        if (isConfigurationCandidate(metadata)) {
            return true;
        }
        return false;
    }
}
