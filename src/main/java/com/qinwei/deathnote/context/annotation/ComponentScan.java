package com.qinwei.deathnote.context.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qinwei
 * @date 2019-06-19
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ComponentScan {

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    ScopedProxyMode scopedProxy() default ScopedProxyMode.DEFAULT;

    boolean useDefaultFilters() default true;

    Class<? extends Annotation>[] includeFilters() default {};

    Class<? extends Annotation>[] excludeFilters() default {};

}
