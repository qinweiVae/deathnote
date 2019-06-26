package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.beans.factory.BeanFactory;
import com.qinwei.deathnote.context.aware.BeanFactoryAware;
import com.qinwei.deathnote.context.metadata.AnnotationMetadata;

import java.util.Arrays;

/**
 * @author qinwei
 * @date 2019-06-26
 */
public class ImportSelectorTest implements ImportSelector, BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        String[] imports = {Application.class.getName()};
        System.out.println(Arrays.toString(imports));
        return imports;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}
