package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.context.metadata.AnnotationMetadata;

/**
 * @author qinwei
 * @date 2019-06-20
 */
public interface ImportSelector {

    /**
     * 获取 class name
     */
    String[] selectImports(AnnotationMetadata importingClassMetadata);
}
