package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.beans.bean.AnnotatedBeanDefinition;
import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.context.metadata.ScopeMetadata;

import java.lang.annotation.Annotation;

/**
 * @author qinwei
 * @date 2019-06-17
 */
public class AnnotationScopeMetadataResolver implements ScopeMetadataResolver {

    private final ScopedProxyMode defaultProxyMode;

    protected Class<? extends Annotation> scopeAnnotationType = Scope.class;


    public AnnotationScopeMetadataResolver() {
        this.defaultProxyMode = ScopedProxyMode.NO;
    }

    public AnnotationScopeMetadataResolver(ScopedProxyMode defaultProxyMode) {
        assert defaultProxyMode != null : "'defaultProxyMode' must not be null";
        this.defaultProxyMode = defaultProxyMode;
    }


    public void setScopeAnnotationType(Class<? extends Annotation> scopeAnnotationType) {
        assert scopeAnnotationType != null : "'scopeAnnotationType' must not be null";
        this.scopeAnnotationType = scopeAnnotationType;
    }


    /**
     * 解析 @scope 注解
     */
    @Override
    public ScopeMetadata resolveScopeMetadata(BeanDefinition definition) {
        ScopeMetadata metadata = new ScopeMetadata();
        if (definition instanceof AnnotatedBeanDefinition) {
            AnnotatedBeanDefinition abd = (AnnotatedBeanDefinition) definition;
            AnnotationAttributes attributes = AnnotationAttributes.fromMap(abd.getMetadata().getAnnotationAttributes(this.scopeAnnotationType.getName()));
            if (attributes != null) {
                metadata.setScopeName(attributes.getString("value"));
                ScopedProxyMode proxyMode = attributes.getEnum("proxyMode");
                if (proxyMode == ScopedProxyMode.DEFAULT) {
                    proxyMode = this.defaultProxyMode;
                }
                metadata.setScopedProxyMode(proxyMode);
            }
        }
        return metadata;
    }

}
