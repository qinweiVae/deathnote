package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.context.metadata.ScopeMetadata;

/**
 * @author qinwei
 * @date 2019-06-17
 */
public interface ScopeMetadataResolver {

    ScopeMetadata resolveScopeMetadata(BeanDefinition definition);
}
