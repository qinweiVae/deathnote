package com.qinwei.deathnote.tx.annotation;

import com.qinwei.deathnote.context.annotation.AnnotationAttributes;
import com.qinwei.deathnote.context.annotation.AnnotationConfigUtils;
import com.qinwei.deathnote.context.annotation.ImportSelector;
import com.qinwei.deathnote.context.metadata.AnnotationMetadata;
import com.qinwei.deathnote.context.support.ResolveType;

import java.lang.annotation.Annotation;

/**
 * @author qinwei
 * @date 2019-07-30
 */
public abstract class AdviceModeImportSelector<A extends Annotation> implements ImportSelector {

    public static final String ADVICE_MODE_ATTRIBUTE_NAME = "mode";

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        //拿到泛型，即注解的类型
        Class annotationType = ResolveType.forType(getClass()).resolveClass(0);
        //拿到 注解 的属性
        AnnotationAttributes attributes = AnnotationConfigUtils.attributesFor(importingClassMetadata, annotationType);
        if (attributes == null) {
            throw new IllegalArgumentException(String.format("@%s is not present on importing class '%s' as expected", annotationType.getSimpleName(), importingClassMetadata.getClassName()));
        }
        AdviceMode adviceMode = attributes.getEnum(ADVICE_MODE_ATTRIBUTE_NAME);
        String[] imports = selectImports(adviceMode);
        if (imports == null) {
            throw new IllegalArgumentException("Unknown AdviceMode: " + adviceMode);
        }
        return imports;
    }

    protected abstract String[] selectImports(AdviceMode adviceMode);
}
