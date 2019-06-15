package com.qinwei.deathnote.context.annotation;

/**
 * @author qinwei
 * @date 2019-06-13
 */
public interface AnnotationConfigRegistry {

    void register(Class<?>... annotatedClass);

    void scan(String... basePackages);
}
