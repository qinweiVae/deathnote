package com.qinwei.deathnote.tx.annotation;

import com.qinwei.deathnote.aop.support.AopUtils;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.context.annotation.AnnotationAttributes;
import com.qinwei.deathnote.context.annotation.AnnotationConfigUtils;
import com.qinwei.deathnote.context.annotation.ImportBeanDefinitionRegistrar;
import com.qinwei.deathnote.context.metadata.AnnotationMetadata;

import java.util.Set;

/**
 * @author qinwei
 * @date 2019-07-30
 */
public class AutoProxyRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Set<String> annotationTypes = importingClassMetadata.getAnnotationTypes();

        for (String type : annotationTypes) {

            AnnotationAttributes candidate = AnnotationConfigUtils.attributesFor(importingClassMetadata, type);
            if (candidate == null) {
                continue;
            }

            Object mode = candidate.get("mode");
            Object proxyTargetClass = candidate.get("proxyTargetClass");

            if (mode != null && proxyTargetClass != null &&
                    AdviceMode.class == mode.getClass() &&
                    Boolean.class == proxyTargetClass.getClass()) {

                if (mode == AdviceMode.PROXY) {
                    AopUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);
                    if ((Boolean) proxyTargetClass) {
                        AopUtils.forceAutoProxyCreatorToUseClassProxying(registry);
                        return;
                    }
                }

            }
        }
    }
}
