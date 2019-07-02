package com.qinwei.deathnote.beans;

import com.qinwei.deathnote.beans.bean.AbstractBeanDefinition;
import com.qinwei.deathnote.beans.bean.BeanDefinition;
import com.qinwei.deathnote.beans.bean.DisposableBeanAdapter;
import com.qinwei.deathnote.beans.bean.SmartInitializingSingleton;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.context.support.ResolveType;
import com.qinwei.deathnote.utils.AnnotationUtils;
import com.qinwei.deathnote.utils.CollectionUtils;
import com.qinwei.deathnote.utils.ObjectUtils;
import com.qinwei.deathnote.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.*;
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

    /**
     * 初始化所有非lazy的单例bean,并执行SmartInitializingSingleton 的扩展
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
        for (String beanName : beanNames) {
            Object singleton = getSingleton(beanName);
            if (singleton instanceof SmartInitializingSingleton) {
                SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singleton;
                smartSingleton.afterSingletonsInstantiated();
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
            annotation = AnnotationUtils.findAnnotation(type, annotationType);
        }
        if (type == null && containsBeanDefinition(beanName)) {
            BeanDefinition bd = getBeanDefinition(beanName);
            if (bd instanceof AbstractBeanDefinition) {
                AbstractBeanDefinition abd = (AbstractBeanDefinition) bd;
                if (abd.hasBeanClass()) {
                    annotation = AnnotationUtils.findAnnotation(abd.getBeanClass(), annotationType);
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

    @Override
    public void updateBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        this.beanDefinitionMap.put(beanName, beanDefinition);
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

    /**
     * 根据类型解析beanName的依赖
     */
    @Override
    public Object resolveDependency(String beanName, ResolveType resolveType, Set<String> autowiredBeanNames) {
        // 解析数组、list、map 等类型的依赖
        Object multipleBeans = resolveMultipleBeans(beanName, resolveType, autowiredBeanNames);
        if (multipleBeans != null) {
            return multipleBeans;
        }
        //解析普通类型（非集合类型）的依赖
        Set<String> matchingBeanNames = findAutowireCandidates(beanName, resolveType.getType());
        if (CollectionUtils.isEmpty(matchingBeanNames)) {
            return null;
        }
        String autowiredBeanName;
        Object instance;
        //刚好只找到一个bean
        if (matchingBeanNames.size() == 1) {
            autowiredBeanName = matchingBeanNames.iterator().next();
            instance = getBean(autowiredBeanName);
        }
        //如果找到多个bean
        else {
            autowiredBeanName = determineAutowireCandidate(matchingBeanNames, resolveType);
            if (autowiredBeanName == null) {
                return null;
            }
            instance = getBean(autowiredBeanName);
        }
        if (autowiredBeanNames != null) {
            autowiredBeanNames.add(autowiredBeanName);
        }
        return instance;
    }

    /**
     * 找到合适的beanName
     */
    protected String determineAutowireCandidate(Set<String> beanNames, ResolveType resolveType) {
        //选择 primary 的bean
        String primaryBean = determinePrimaryBean(StringUtils.toArray(beanNames), resolveType.getType());
        if (primaryBean != null) {
            return primaryBean;
        }
        //如果没有找到 primary 的 beanName，则寻找和 属性名称匹配的 beanName
        for (String name : beanNames) {
            String candidateName = name;
            Object beanInstance = getBean(name);
            if (beanInstance != null && matchesBeanName(candidateName, resolveType.getName())) {
                return candidateName;
            }
        }
        return null;
    }

    /**
     * 判断beanName和candidateName是否匹配(2个名称相同或者 candidateName是beanName的别名)
     */
    protected boolean matchesBeanName(String beanName, String candidateName) {
        return beanName.equals(candidateName) || ObjectUtils.containsElement(getAliases(beanName), candidateName);
    }

    /**
     * 解析Array、Collection、Map 等类型
     */
    private Object resolveMultipleBeans(String beanName, ResolveType resolveType, Set<String> autowiredBeanNames) {
        Class<?> type = resolveType.getType();
        if (type.isArray()) {
            //获取数组中元素的类型
            Class<?> componentType = type.getComponentType();
            if (componentType == null) {
                return null;
            }
            Set<String> matchingBeanNames = findAutowireCandidates(beanName, componentType);
            if (matchingBeanNames.isEmpty()) {
                return null;
            }
            if (autowiredBeanNames != null) {
                autowiredBeanNames.addAll(matchingBeanNames);
            }
            Collection<Object> values = new ArrayList<>();
            for (String name : matchingBeanNames) {
                values.add(getBean(name));
            }
            //如果集合内的元素无法转换直接抛异常
            if (!getConversion().canConvert(values.iterator().next().getClass(), componentType)) {
                throw new IllegalStateException("Unable to convert " + values.iterator().next().getClass().getName() + " to " + componentType.getName() + " , beanName : " + beanName);
            }
            return convertToArray(componentType, values);
        } else if (Collection.class.isAssignableFrom(type) && type.isInterface()) {
            //集合中元素的类型,这里不支持集合的嵌套
            Class elementType = resolveType.resolveSpecialType(0);
            if (elementType == null) {
                return null;
            }
            Set<String> matchingBeanNames = findAutowireCandidates(beanName, elementType);
            if (matchingBeanNames.isEmpty()) {
                return null;
            }
            if (autowiredBeanNames != null) {
                autowiredBeanNames.addAll(matchingBeanNames);
            }
            Collection<Object> values = new ArrayList<>();
            for (String name : matchingBeanNames) {
                values.add(getBean(name));
            }
            //如果集合内的元素无法转换直接抛异常
            if (!getConversion().canConvert(values.iterator().next().getClass(), elementType)) {
                throw new IllegalStateException("Unable to convert " + values.iterator().next().getClass().getName() + " to " + elementType.getName() + " , beanName : " + beanName);
            }
            return convertToCollection(type, elementType, values);
        } else if (Map.class == type) {
            //map 中 key 的类型
            Class keyType = resolveType.resolveSpecialType(0);
            //map 中 value 的类型，不支持嵌套
            Class valueType = resolveType.resolveSpecialType(1);
            //key必须是string 类型
            if (keyType == null || String.class != keyType) {
                return null;
            }
            if (valueType == null) {
                return null;
            }
            Set<String> matchingBeanNames = findAutowireCandidates(beanName, valueType);
            if (matchingBeanNames.isEmpty()) {
                return null;
            }
            if (autowiredBeanNames != null) {
                autowiredBeanNames.addAll(matchingBeanNames);
            }
            Map result = new HashMap();
            for (String name : matchingBeanNames) {
                result.put(name, getBean(name));
            }
            return result;
        }
        return null;
    }

    /**
     * 找到符合条件的bean name
     */
    private Set<String> findAutowireCandidates(String beanName, Class<?> type) {
        //获取所有指定类型的bean的beanName,包括非单例bean
        String[] candidateNames = getBeanNamesForType(type);
        Set<String> result = new HashSet<>(candidateNames.length);
        for (String name : candidateNames) {
            // beanName 不能与 name 相同,不然就是循环引用了
            if (!name.equals(beanName)) {
                result.add(name);
            }
        }
        return result;
    }

    /**
     * Collection 转成 Arrary
     */
    private Object convertToArray(Class<?> componentType, Collection<Object> values) {
        Object array = Array.newInstance(componentType, values.size());
        int i = 0;
        for (Object value : values) {
            Array.set(array, i++, getConversion().convertIfNecessary(value, componentType));
        }
        return array;
    }

    /**
     * 转成对应的collection
     */
    private <T> Collection convertToCollection(Class<?> type, Class<T> elementType, Collection<Object> values) {
        Collection<T> collection;
        if (List.class == type) {
            collection = new ArrayList();
        } else if (Set.class == type || Collection.class == type) {
            collection = new LinkedHashSet();
        } else if (SortedSet.class == type || NavigableSet.class == type) {
            collection = new TreeSet();
        } else {
            return null;
        }
        for (Object value : values) {
            collection.add(getConversion().convertIfNecessary(value, elementType));
        }
        return collection;
    }
}
