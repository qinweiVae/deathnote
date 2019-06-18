package com.qinwei.deathnote.beans.bean;

import com.qinwei.deathnote.context.metadata.AnnotationMetadata;

/**
 * @author qinwei
 * @date 2019-06-13
 */
public interface AnnotatedBeanDefinition extends BeanDefinition {

    AnnotationMetadata getMetadata();
}
