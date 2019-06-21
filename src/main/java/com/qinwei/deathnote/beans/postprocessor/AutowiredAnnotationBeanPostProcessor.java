package com.qinwei.deathnote.beans.postprocessor;

import com.qinwei.deathnote.beans.factory.BeanFactory;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.config.Config;
import com.qinwei.deathnote.context.annotation.AnnotationAttributes;
import com.qinwei.deathnote.context.annotation.Autowired;
import com.qinwei.deathnote.context.annotation.Value;
import com.qinwei.deathnote.context.aware.BeanFactoryAware;
import com.qinwei.deathnote.context.aware.ConfigAware;
import com.qinwei.deathnote.context.support.ResolveType;
import com.qinwei.deathnote.support.resolve.DefaultPropertyResolver;
import com.qinwei.deathnote.support.resolve.PropertyResolver;
import com.qinwei.deathnote.utils.AnnotationUtils;
import com.qinwei.deathnote.utils.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-18
 */
public class AutowiredAnnotationBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware, ConfigAware {

    private ConfigurableListableBeanFactory beanFactory;

    private Config config;

    private PropertyResolver propertyResolver = new DefaultPropertyResolver();

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
            throw new IllegalArgumentException("AutowiredAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory: " + beanFactory);
        }
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    @Override
    public Map<String, Object> postProcessProperties(Map<String, Object> properties, Object bean, String beanName) {
        try {
            //属性注入
            injectField(bean, beanName);
            //方法注入
            injectMethod(bean, beanName);

        } catch (Exception e) {
            throw new RuntimeException("Inject failure ...", e);
        }
        return properties;
    }

    /**
     * Field 注入
     */
    private void injectField(Object bean, String beanName) throws Exception {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            //处理 Field 上的  @Autowired 注解
            processAutowired(bean, beanName, field);
            //处理 Field 上的  @Value 注解
            processValue(bean, field);
        }
    }

    /**
     * Method 注入
     */
    private void injectMethod(Object bean, String beanName) throws Exception {
        Method[] methods = bean.getClass().getDeclaredMethods();
        for (Method method : methods) {
            //处理 Method 上的  @Autowired 和 @Value 注解
            processAutowired(bean, beanName, method);
        }
    }

    /**
     * 处理 Method 上的  @Autowired 注解
     */
    private void processAutowired(Object bean, String beanName, Method method) throws Exception {
        AnnotationAttributes attributes = findAnnotationAttributes(method, Autowired.class);
        if (attributes != null) {
            boolean required = attributes.getBoolean("required");
            // 方法中的所有参数
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] arguments = new Object[parameterTypes.length];
            Set<String> autowiredBeanNames = new LinkedHashSet<>(parameterTypes.length);

            for (int i = 0; i < arguments.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                Object arg = null;
                if (ClassUtils.isSimpleProperty(parameterType)) {
                    //获取 所有方法参数的所有注解
                    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                    //拿到当前方法参数的 所有注解
                    Annotation[] annotations = parameterAnnotations[i];
                    for (Annotation annotation : annotations) {
                        if (annotation.annotationType() != Value.class) {
                            continue;
                        }
                        AnnotationAttributes valueAttr = AnnotationUtils.getAnnotationAttributes(annotation);
                        if (valueAttr != null) {
                            String value = attributes.getString("value");
                            // 解析 占位符
                            value = propertyResolver.resolvePlaceholders(value, config.getProperties());
                            // 类型转换
                            Object resolvedValue = this.beanFactory.getConversion().convertIfNecessary(value, parameterType);
                            // 如果没有找到 value 的值，并且是基本类型，则赋默认值
                            arg = resolvedValue == null ? ClassUtils.DEFAULT_TYPE_VALUES.get(parameterType) : resolvedValue;
                        }
                    }
                } else {
                    // 根据类型解析bean的依赖,从所有的bean中找到合适的bean注入到 方法参数
                    arg = this.beanFactory.resolveDependency(beanName, ResolveType.forType(parameterType), autowiredBeanNames);
                }
                if (arg == null && required) {
                    throw new IllegalStateException("Unable to find a appropriate bean for the Method [" + method.getName() + "]  ParameterType [" + parameterType.getName() + "] in " + bean.getClass().getName());
                }
                arguments[i] = arg;
            }
            // 注册bean的依赖关系
            registerDependentBean(beanName, autowiredBeanNames);
            ClassUtils.makeAccessible(method);
            method.invoke(bean, arguments);
        }
    }

    /**
     * 处理 Field 上的  @Value 注解
     */
    private void processValue(Object bean, Field field) throws Exception {
        // 如果不属于简单类型，则不处理 
        if (!ClassUtils.isSimpleProperty(field.getType())) {
            return;
        }
        AnnotationAttributes attributes = findAnnotationAttributes(field, Value.class);
        if (attributes != null) {
            String value = attributes.getString("value");
            // 解析 占位符
            value = propertyResolver.resolvePlaceholders(value, config.getProperties());
            // 类型转换
            Object resolvedValue = this.beanFactory.getConversion().convertIfNecessary(value, field.getType());
            if (resolvedValue != null) {
                ClassUtils.makeAccessible(field);
                field.set(bean, resolvedValue);
            }
        }
    }

    /**
     * 处理 Field 上的  @Autowired 注解
     */
    private void processAutowired(Object bean, String beanName, Field field) throws Exception {
        // 如果属于简单类型，则不处理 
        if (ClassUtils.isSimpleProperty(field.getType())) {
            return;
        }
        AnnotationAttributes autowired = findAnnotationAttributes(field, Autowired.class);
        if (autowired != null) {
            boolean required = autowired.getBoolean("required");
            Set<String> autowiredBeanNames = new LinkedHashSet<>(4);
            // 根据类型解析bean的依赖,从所有的bean中找到合适的bean注入到 field
            Object value = this.beanFactory.resolveDependency(beanName, ResolveType.forType(field), autowiredBeanNames);
            if (value == null && required) {
                throw new IllegalStateException("Unable to find a appropriate bean for the Field [" + field.getName() + "] in " + bean.getClass().getName());
            }
            if (value != null) {
                // 注册bean的依赖关系
                registerDependentBean(beanName, autowiredBeanNames);
                ClassUtils.makeAccessible(field);
                field.set(bean, value);
            }
        }
    }

    /**
     * 注册bean的依赖关系
     */
    private void registerDependentBean(String beanName, Set<String> autowiredBeanNames) {
        if (beanName == null) {
            return;
        }
        for (String autowiredBeanName : autowiredBeanNames) {
            if (this.beanFactory != null && this.beanFactory.containsBean(autowiredBeanName)) {
                this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
            }
        }
    }

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    private AnnotationAttributes findAnnotationAttributes(AnnotatedElement element, Class<? extends Annotation> annotation) {
        return AnnotationUtils.getAnnotationAttributes(element, annotation.getName(), false);
    }
}
