package com.qinwei.deathnote.beans;

import com.qinwei.deathnote.beans.bean.AbstractBeanDefinition;
import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.beans.bean.DisposableBeanAdapter;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
        implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

    private volatile List<String> beanDefinitionNames = new ArrayList<>(256);

    private final Map<Class<?>, String[]> allBeanNamesByType = new ConcurrentHashMap<>(64);

    private final Map<Class<?>, String[]> singletonBeanNamesByType = new ConcurrentHashMap<>(64);

    /**
     * 初始化所有非lazy的单例bean
     */
    @Override
    public void preInstantiateSingletons() {
        List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);
        for (String beanName : beanNames) {
            BeanDefinition bd = this.beanDefinitionMap.get(beanName);
            if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
                getBean(beanName);
            }
        }
    }

    /**
     * 销毁bean
     */
    @Override
    public void destroyBean(String beanName, Object beanInstance) {
        destroyBean(beanName, beanInstance, getBeanDefinition(beanName));
    }

    /**
     * 执行DestructionAwareBeanPostProcessor的postProcessBeforeDestruction()方法
     * 执行 DisposableBean 的destroy()方法
     * 如果bean包含无参的destroyMethod，则执行其destroyMethod
     */
    protected void destroyBean(String beanName, Object bean, BeanDefinition bd) {
        new DisposableBeanAdapter(bean, beanName, bd, getBeanPostProcessors()).destroy();
    }

    /**
     * 获取所有指定类型的bean的beanName,包括非单例bean
     */
    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        return getBeanNamesForType(type, true);
    }

    /**
     * 获取所有指定类型的bean的beanName,并缓存起来,根据includeNonSingletons判断是否包括非单例bean
     */
    @Override
    public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons) {
        if (type == null) {
            return doGetBeanNamesForType(type, includeNonSingletons);
        }
        Map<Class<?>, String[]> cache = includeNonSingletons ? this.allBeanNamesByType : this.singletonBeanNamesByType;
        String[] result = cache.get(type);
        if (result != null) {
            return result;
        }
        result = doGetBeanNamesForType(type, includeNonSingletons);
        cache.put(type, result);
        return result;
    }

    /**
     * 获取所有指定类型的bean的beanName,根据includeNonSingletons判断是否包括非单例bean
     */
    private String[] doGetBeanNamesForType(Class<?> type, boolean includeNonSingletons) {
        List<String> result = new ArrayList<>();
        for (String beanName : this.beanDefinitionNames) {
            if (!isAlias(beanName)) {
                BeanDefinition bd = getBeanDefinition(beanName);
                boolean match = (includeNonSingletons || bd.isSingleton()) && isTypeMatch(beanName, type);
                if (match) {
                    result.add(beanName);
                }
            }
        }
        return StringUtils.toArray(result);
    }

    /**
     * 获取所有属于指定类型的bean (包括单例和原型)， beanName ---> bean
     */
    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        return getBeansOfType(type, true);
    }

    /**
     * 获取所有属于指定类型的bean，根据includeNonSingletons 决定是否包含原型bean， beanName ---> bean
     */
    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons) {
        String[] beanNames = getBeanNamesForType(type, includeNonSingletons);
        return Arrays.stream(beanNames)
                .collect(Collectors.toMap(beanName -> beanName, beanName -> (T) getBean(beanName), (a, b) -> b, () -> new LinkedHashMap<>(beanNames.length)));
    }

    /**
     * 获取所有包含注解的bean， beanName ---> bean
     */
    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
        String[] beanNames = getBeanNamesForAnnotation(annotationType);
        return Arrays.stream(beanNames)
                .collect(Collectors.toMap(beanName -> beanName, beanName -> getBean(beanName), (a, b) -> b, () -> new LinkedHashMap<>(beanNames.length)));
    }

    /**
     * 寻找bean上的指定注解，当前bean没有，则从其所有的注解中寻找，再从其所有接口中寻找，再从其所有父类中寻找
     */
    @Override
    public <T extends Annotation> T findAnnotationOnBean(String beanName, Class<T> annotationType) {
        T annotation = null;
        Class<?> type = getType(beanName);
        if (type != null) {
            annotation = ClassUtils.findAnnotation(type, annotationType);
        }
        if (type == null && containsBeanDefinition(beanName)) {
            BeanDefinition bd = getBeanDefinition(beanName);
            if (bd instanceof AbstractBeanDefinition) {
                AbstractBeanDefinition abd = (AbstractBeanDefinition) bd;
                if (abd.hasBeanClass()) {
                    annotation = ClassUtils.findAnnotation(abd.getBeanClass(), annotationType);
                }
            }
        }
        return annotation;
    }

    /**
     * 获取包含指定注解的bean
     */
    @Override
    public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
        List<String> result = new ArrayList<>();
        for (String beanName : this.beanDefinitionNames) {
            BeanDefinition bd = getBeanDefinition(beanName);
            if (!bd.isAbstract() && findAnnotationOnBean(beanName, annotationType) != null) {
                result.add(beanName);
            }
        }
        return StringUtils.toArray(result);
    }

    /**
     * 根据 类型 获取 bean，如果只有一个则直接返回，有多个的话返回 primary的bean
     */
    @Override
    public <T> T getBean(Class<T> requiredType) {
        String[] beanNames = getBeanNamesForType(requiredType);
        if (beanNames.length == 1) {
            return getBean(beanNames[0], requiredType);
        }
        if (beanNames.length > 1) {
            String beanName = determinePrimaryBean(beanNames, requiredType);
            if (beanName == null) {
                throw new IllegalStateException("No unique BeanDefinition found on type : " + requiredType);
            }
            return getBean(beanName, requiredType);
        }
        return null;
    }

    /**
     * 选择 primary 的bean,只能有一个primary
     */
    private <T> String determinePrimaryBean(String[] beanNames, Class<T> requiredType) {
        String primaryBeanName = null;
        for (String candidateBeanName : beanNames) {
            BeanDefinition bd = getBeanDefinition(candidateBeanName);
            if (bd.isPrimary()) {
                if (primaryBeanName != null) {
                    boolean candidate = containsBeanDefinition(candidateBeanName);
                    boolean primary = containsBeanDefinition(primaryBeanName);
                    if (primary && candidate) {
                        throw new IllegalStateException("more than one 'primary' beans found on class type : " + requiredType);
                    } else if (candidate) {
                        primaryBeanName = candidateBeanName;
                    }
                } else {
                    primaryBeanName = candidateBeanName;
                }
            }
        }
        return primaryBeanName;
    }

    /**
     * 注册 BeanDefinition
     */
    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        assert !StringUtils.isEmpty(beanName) : "beanName must not be null";
        assert beanDefinition != null : "beanDefinition must not be null";
        BeanDefinition existingDefinition = this.beanDefinitionMap.get(beanName);
        if (existingDefinition != null) {
            throw new UnsupportedOperationException("Cannot register beans definition [" + beanDefinition + "] for beans '" + beanName +
                    "': There is already [" + existingDefinition + "] bound.");
        }
        this.beanDefinitionMap.put(beanName, beanDefinition);
        this.beanDefinitionNames.add(beanName);

    }

    /**
     * 根据 beanName 移除 BeanDefinition
     */
    @Override
    public void removeBeanDefinition(String beanName) {
        assert !StringUtils.isEmpty(beanName) : "beanName must not be null";
        BeanDefinition bd = this.beanDefinitionMap.get(beanName);
        if (bd == null) {
            throw new IllegalArgumentException("No beans named '" + beanName + "' available");
        }
        this.beanDefinitionNames.remove(beanName);
    }

    /**
     * 根据 beanName 获取 BeanDefinition
     */
    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        assert !StringUtils.isEmpty(beanName) : "beanName must not be null";
        BeanDefinition bd = this.beanDefinitionMap.get(beanName);
        if (bd == null) {
            throw new IllegalArgumentException("No beans named '" + beanName + "' available");
        }
        return bd;
    }

    /**
     * 根据 beanName 判断是否包含 BeanDefinition
     */
    @Override
    public boolean containsBeanDefinition(String beanName) {
        assert !StringUtils.isEmpty(beanName) : "beanName must not be null";
        return this.beanDefinitionMap.containsKey(beanName);
    }

    /**
     * 获取所有的 beanName
     */
    @Override
    public String[] getBeanDefinitionNames() {
        return StringUtils.toArray(this.beanDefinitionNames);
    }

    /**
     * 获取 BeanDefinition 的数量
     */
    @Override
    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

}
