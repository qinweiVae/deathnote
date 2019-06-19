package com.qinwei.deathnote.context.support;

import com.qinwei.deathnote.beans.bean.AnnotatedBeanDefinition;
import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.context.annotation.AnnotationAttributes;
import com.qinwei.deathnote.context.annotation.AnnotationConfigUtils;
import com.qinwei.deathnote.context.metadata.AnnotationMetadata;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.StringUtils;

import java.beans.Introspector;
import java.util.Map;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-13
 */
public class AnnotationBeanNameGenerator implements BeanNameGenerator {


    public static final String COMPONENT_ANNOTATION_CLASSNAME = "com.qinwei.deathnote.context.annotation.Component";

    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        if (definition instanceof AnnotatedBeanDefinition) {
            String beanName = determineBeanNameFromAnnotation((AnnotatedBeanDefinition) definition);
            if (StringUtils.isNotEmpty(beanName)) {
                return beanName;
            }
        }
        return buildDefaultBeanName(definition);
    }

    /**
     * 从 bean注解中找到 @Component 或者 @Named 注解，用其 value 里面的值 作为 beanName
     */
    private String determineBeanNameFromAnnotation(AnnotatedBeanDefinition abd) {
        AnnotationMetadata metadata = abd.getMetadata();
        // 获取 所有的 注解名称
        Set<String> types = metadata.getAnnotationTypes();
        String beanName = null;
        for (String type : types) {
            AnnotationAttributes attributes = AnnotationConfigUtils.attributesFor(metadata, type);
            //判断是否含有 @Component 或者 @Named 注解
            if (attributes != null && isStereotypeWithNameValue(type, metadata.getMetaAnnotationTypes(type), attributes)) {
                Object value = attributes.get("value");
                if (value instanceof String) {
                    String strVal = (String) value;
                    if (StringUtils.isNotEmpty(strVal)) {
                        if (beanName != null && !strVal.equals(beanName)) {
                            throw new IllegalStateException("Stereotype annotations suggest inconsistent component names: '" + beanName + "' versus '" + strVal + "'");
                        }
                        beanName = strVal;
                    }
                }
            }
        }
        return beanName;
    }

    /**
     * 判断是否含有 @Component 或者 @Named 注解
     */
    protected boolean isStereotypeWithNameValue(String annotationType, Set<String> metaAnnotationTypes, Map<String, Object> attributes) {
        boolean isStereotype = annotationType.equals(COMPONENT_ANNOTATION_CLASSNAME) ||
                metaAnnotationTypes.contains(COMPONENT_ANNOTATION_CLASSNAME) ||
                annotationType.equals("javax.inject.Named");

        return (isStereotype && attributes != null && attributes.containsKey("value"));
    }

    protected String buildDefaultBeanName(BeanDefinition definition) {
        String beanClassName = definition.getBeanClassName();
        String shortClassName = ClassUtils.getShortName(beanClassName);
        //首字母转小写，如果第1个和第2个字符都是大写，则不转换
        return Introspector.decapitalize(shortClassName);
    }
}
