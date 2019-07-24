package com.qinwei.deathnote.beans;

import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.beans.bean.FactoryBean;
import com.qinwei.deathnote.beans.bean.RootBeanDefinition;
import com.qinwei.deathnote.beans.factory.ConfigurableBeanFactory;
import com.qinwei.deathnote.beans.postprocessor.BeanPostProcessor;
import com.qinwei.deathnote.beans.postprocessor.DestructionAwareBeanPostProcessor;
import com.qinwei.deathnote.beans.postprocessor.InstantiationAwareBeanPostProcessor;
import com.qinwei.deathnote.beans.registry.DefaultSingletonBeanRegistry;
import com.qinwei.deathnote.support.convert.Conversion;
import com.qinwei.deathnote.support.convert.DefaultConversion;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.StringUtils;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements ConfigurableBeanFactory {

    private final List<BeanPostProcessor> beanPostProcessors = new CopyOnWriteArrayList<>();

    private volatile boolean hasInstantiationAwareBeanPostProcessors;

    private volatile boolean hasDestructionAwareBeanPostProcessors;

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        assert beanPostProcessor != null : "BeanPostProcessor must not be null";
        beanPostProcessors.remove(beanPostProcessor);
        if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
            this.hasInstantiationAwareBeanPostProcessors = true;
        }
        if (beanPostProcessor instanceof DestructionAwareBeanPostProcessor) {
            this.hasDestructionAwareBeanPostProcessors = true;
        }
        beanPostProcessors.add(beanPostProcessor);
    }

    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    protected boolean hasInstantiationAwareBeanPostProcessors() {
        return this.hasInstantiationAwareBeanPostProcessors;
    }

    protected boolean hasDestructionAwareBeanPostProcessors() {
        return this.hasDestructionAwareBeanPostProcessors;
    }

    /**
     * 根据name获取bean,没有的话会创建
     */
    @Override
    public Object getBean(String name) {
        return doGetBean(name, null, null);
    }

    /**
     * 根据name获取bean,没有的话会创建，并使用强转或者类型转换器将bean转换成指定的类型
     */
    @Override
    public <T> T getBean(String name, Class<T> requiredType) {
        return doGetBean(name, requiredType, null);
    }

    /**
     * 获取bean,有则直接返回，没有则创建bean后返回
     */
    protected <T> T doGetBean(final String name, final Class<T> requiredType, final Object[] args) {
        assert !StringUtils.isEmpty(name) : "name must not be null";
        Object bean = null;
        String beanName = transformedBeanName(name);
        Object sharedInstance = getSingleton(beanName);
        if (sharedInstance != null) {
            // 如果是 FactoryBean 的话，调用其 getObject()方法得到真实的bean
            bean = getObjectForBeanInstance(sharedInstance, name, beanName);
        } else {
            BeanDefinition bd = getBeanDefinition(beanName);
            if (bd.isAbstract()) {
                throw new IllegalStateException("Bean definition is abstract, beanName : " + beanName);
            }
            String[] dependsOn = bd.getDependsOn();
            if (dependsOn != null) {
                for (String depend : dependsOn) {
                    //判断depend 是否依赖于beanName (是否循环依赖)
                    if (isDependent(beanName, depend)) {
                        throw new IllegalStateException("Circular depends-on relationship between '" + beanName + "' and '" + depend + "'");
                    }
                    //注册bean的依赖关系(beanName 依赖于 depend)
                    registerDependentBean(depend, beanName);
                    try {
                        //创建bean
                        getBean(depend);
                    } catch (Exception e) {
                        throw new RuntimeException("'" + beanName + "' depends on missing beans '" + depend + "'", e);
                    }
                }
            }
            // 如果是 AnnotatedBeanDefinition，转换成 RootBeanDefinition
            if (!(bd instanceof RootBeanDefinition)) {
                bd = new RootBeanDefinition(bd);
            }
            RootBeanDefinition rbd = (RootBeanDefinition) bd;
            //如果是单例
            if (bd.isSingleton()) {
                sharedInstance = getSingleton(beanName, () -> createBean(beanName, rbd, args));
                bean = getObjectForBeanInstance(sharedInstance, name, beanName);
            }
            //如果是原型
            else if (bd.isPrototype()) {
                Object prototypeInstance;
                try {
                    beforePrototypeCreation(beanName);
                    prototypeInstance = createBean(beanName, rbd, args);
                } finally {
                    afterPrototypeCreation(beanName);
                }
                if (prototypeInstance != null) {
                    bean = getObjectForBeanInstance(prototypeInstance, name, beanName);
                }
            }
        }
        //如果 bean 是 jdk 代理类, requiredType 只能是 原始对象的 接口，否则会转换失败
        if (Proxy.isProxyClass(bean.getClass())) {
            return (T) bean;
        }
        //普通的bean 和 CGLib代理 的bean ， 可以 使用类型转换器进行转换
        if (requiredType != null) {
            return getConversion().convertIfNecessary(bean, requiredType);
        }
        return (T) bean;
    }

    /**
     * 如果是 FactoryBean 的话，调用其 getObject()方法得到真实的bean
     */
    protected Object getObjectForBeanInstance(Object beanInstance, String name, String beanName) {
        // 如果 name 是以 & 开头，说明是 FactoryBean
        if (name.startsWith(FACTORY_BEAN_NAME)) {
            if (!(beanInstance instanceof FactoryBean)) {
                throw new IllegalStateException("非FactoryBean 名称不能使用 '&' 开头 --- beanName : " + beanName + " , class : " + beanInstance.getClass());
            }
        }
        if (!(beanInstance instanceof FactoryBean) || name.startsWith(FACTORY_BEAN_NAME)) {
            return beanInstance;
        }
        FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
        Object object;
        try {
            object = factory.getObject();
        } catch (Exception e) {
            throw new RuntimeException("Unable to get bean from FactoryBean , beanName : " + beanName + " , class : " + beanInstance.getClass());
        }
        return object;
    }

    /**
     * 原型bean创建前
     */
    protected void beforePrototypeCreation(String beanName) {

    }

    /**
     * 原型bean创建后
     */
    protected void afterPrototypeCreation(String beanName) {

    }

    /**
     * 获取默认的类型转换器
     */
    @Override
    public Conversion getConversion() {
        return DefaultConversion.getInstance();
    }

    /**
     * 获取真正的beanName,如果在AliasRegistry中没有找到，则使用传入的name
     */
    private String transformedBeanName(String name) {
        return canonicalName(name);
    }

    /**
     * 获取所有别名
     */
    @Override
    public String[] getAliases(String name) {
        String beanName = transformedBeanName(name);
        return super.getAliases(beanName);
    }

    /**
     * 判断是否包含单例
     */
    @Override
    public boolean containsBean(String name) {
        String beanName = transformedBeanName(name);
        return super.containsSingleton(beanName);
    }

    /**
     * 解析 beans ，将 beans 类名解析为 class 引用
     */
    protected Class<?> resolveBeanClass(final RootBeanDefinition bd) {
        if (bd.hasBeanClass()) {
            return bd.getBeanClass();
        }
        ClassLoader beanClassLoader = getBeanClassLoader();
        String className = bd.getBeanClassName();
        try {
            return beanClassLoader.loadClass(className);
        } catch (ClassNotFoundException ignore) {

        }
        return bd.resolveBeanClass(beanClassLoader);
    }

    /**
     * 获取默认的ClassLoader
     */
    @Override
    public ClassLoader getBeanClassLoader() {
        return ClassUtils.getDefaultClassLoader();
    }

    /**
     * 判断单例bean的类型是否匹配
     */
    @Override
    public boolean isTypeMatch(String beanName, Class<?> typeToMatch) {
        if (typeToMatch == null) {
            return true;
        }
        Object singleton = getSingleton(beanName);
        if (singleton != null) {
            return typeToMatch.isInstance(singleton) || ClassUtils.isAssignable(typeToMatch, singleton.getClass());
        }
        BeanDefinition bd = getBeanDefinition(beanName);
        Class<?> beanClass = bd.getBeanClass();
        if (beanClass != null) {
            return ClassUtils.isAssignable(typeToMatch, beanClass);
        }
        return false;
    }

    /**
     * 获取单例bean的类型
     */
    @Override
    public Class<?> getType(String name) {
        Object singleton = getSingleton(name);
        if (singleton != null) {
            if (singleton instanceof FactoryBean) {
                return ((FactoryBean<?>) singleton).getObjectType();
            }
            return singleton.getClass();
        }
        BeanDefinition bd = getBeanDefinition(name);
        Class<?> beanClass = bd.getBeanClass();
        if (beanClass != null) {
            return beanClass;
        }
        return null;
    }

    @Override
    public boolean isSingleton(String name) {
        String beanName = transformedBeanName(name);
        Object singleton = getSingleton(beanName);
        if (singleton != null) {
            if (singleton instanceof FactoryBean) {
                return ((FactoryBean<?>) singleton).isSingleton();
            }
            return true;
        }
        BeanDefinition bd = getBeanDefinition(beanName);
        if (bd.isSingleton()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isPrototype(String name) {
        String beanName = transformedBeanName(name);
        Object singleton = getSingleton(beanName);
        if (singleton != null) {
            return false;
        }
        BeanDefinition bd = getBeanDefinition(beanName);
        if (bd.isPrototype()) {
            return true;
        }
        return false;
    }

    protected abstract Object createBean(String beanName, RootBeanDefinition bd, Object[] args);

    protected abstract BeanDefinition getBeanDefinition(String beanName);
}
