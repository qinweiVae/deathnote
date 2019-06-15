package com.qinwei.deathnote.context;

import com.qinwei.deathnote.context.annotation.AnnotationConfigRegistry;

/**
 * @author qinwei
 * @date 2019-06-13
 */
public class AnnotationConfigApplicationContext extends AbstractApplicationContext implements AnnotationConfigRegistry {


    public AnnotationConfigApplicationContext() {
    }

    @Override
    public void register(Class<?>... annotatedClass) {

    }

    @Override
    public void scan(String... basePackages) {

    }
}
