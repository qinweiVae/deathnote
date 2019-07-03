package com.qinwei.deathnote.aop.annotation;

import com.qinwei.deathnote.aop.AspectInstanceFactory;
import com.qinwei.deathnote.aop.BeanFactoryAspectInstanceFactory;
import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.autoproxy.AbstractAdvisorAutoProxyCreator;
import com.qinwei.deathnote.beans.factory.BeanFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author qinwei
 * @date 2019-07-01
 */
public class AnnotationAwareAspectJAutoProxyCreator extends AbstractAdvisorAutoProxyCreator {

    private List<Pattern> includePatterns;

    private AspectJAdvisorFactory advisorFactory;

    private volatile List<String> aspectBeanNames;

    private final Map<String, List<Advisor>> advisorsCache = new ConcurrentHashMap<>();

    private final Map<String, AspectInstanceFactory> aspectCache = new ConcurrentHashMap<>();

    public void setIncludePatterns(List<String> patterns) {
        this.includePatterns = new ArrayList<>(patterns.size());
        for (String patternText : patterns) {
            this.includePatterns.add(Pattern.compile(patternText));
        }
    }

    public void setAdvisorFactory(AspectJAdvisorFactory advisorFactory) {
        assert advisorFactory != null : "AspectJAdvisorFactory must not be null";
        this.advisorFactory = advisorFactory;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
        if (this.advisorFactory == null) {
            this.advisorFactory = new ReflectiveAspectJAdvisorFactory(getBeanFactory());
        }
    }

    /**
     * 判断是否有模式相匹配
     */
    protected boolean isEligibleBean(String beanName) {
        if (this.includePatterns == null) {
            return true;
        }
        for (Pattern pattern : this.includePatterns) {
            if (pattern.matcher(beanName).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<Advisor> findCandidateAdvisors() {
        //从 beanFactory 里面查找所有 Advisor 的bean
        List<Advisor> advisors = super.findCandidateAdvisors();
        // 解析 @Aspect 注解，并构建通知器
        advisors.addAll(buildAspectJAdvisors());
        return advisors;
    }

    /**
     * 解析 @Aspect 注解，并构建通知器
     * <p>
     * 1.获取容器中所有 bean 的名称（beanName）
     * 2.遍历上一步获取到的 bean 名称数组，并获取当前 beanName 对应的 bean 类型（beanType）
     * 3.根据 beanType 判断当前 bean 是否是一个的 Aspect 注解类，若不是则不做任何处理
     * 4.调用 advisorFactory.getAdvisors() 获取通知器
     */
    public List<Advisor> buildAspectJAdvisors() {
        List<String> aspectNames = this.aspectBeanNames;
        if (aspectNames == null) {
            synchronized (this) {
                aspectNames = this.aspectBeanNames;
                if (aspectNames == null) {
                    List<Advisor> advisors = new ArrayList<>();
                    aspectNames = new ArrayList<>();
                    // 从容器中获取所有 bean 的名称
                    String[] beanNames = getBeanFactory().getBeanNamesForType(Object.class);
                    // 遍历 beanNames
                    for (String beanName : beanNames) {
                        //判断是否有模式相匹配
                        if (!isEligibleBean(beanName)) {
                            continue;
                        }
                        // 根据 beanName 获取 bean 的类型
                        Class<?> beanType = getBeanFactory().getType(beanName);
                        if (beanType == null) {
                            continue;
                        }
                        // 检测 beanType 是否包含 Aspect 注解
                        if (this.advisorFactory.isAspect(beanType)) {
                            aspectNames.add(beanName);
                            AspectMetadata metadata = new AspectMetadata(beanType, beanName);
                            // 创建AspectInstanceFactory， 调用getAspectInstance()方法即可以通过 beanFactory.getBean()得到对应的bean
                            AspectInstanceFactory factory = new BeanFactoryAspectInstanceFactory(getBeanFactory(), beanName, metadata);
                            // 解析标记了 AspectJ 注解中的增强方法
                            List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
                            //如果是单例，直接缓存 对应的所有 advisors
                            if (getBeanFactory().isSingleton(beanName)) {
                                this.advisorsCache.put(beanName, classAdvisors);
                            }
                            // 否则缓存 对应的  AspectInstanceFactory
                            else {
                                this.aspectCache.put(beanName, factory);
                            }
                            advisors.addAll(classAdvisors);
                        }
                    }
                    this.aspectBeanNames = aspectNames;
                    return advisors;
                }
            }
        }
        if (aspectNames.isEmpty()) {
            return Collections.emptyList();
        }
        // 如果 aspectBeanNames 存在，则从缓存中取
        List<Advisor> advisors = new ArrayList<>();
        for (String name : aspectNames) {
            //单例
            List<Advisor> cachedAdvisors = this.advisorsCache.get(name);
            if (cachedAdvisors != null) {
                advisors.addAll(cachedAdvisors);
            }
            //原型
            else {
                AspectInstanceFactory factory = this.aspectCache.get(name);
                advisors.addAll(this.advisorFactory.getAdvisors(factory));
            }
        }
        return advisors;
    }

    @Override
    protected boolean isInfrastructureClass(Class<?> beanClass) {
        return super.isInfrastructureClass(beanClass) || (this.advisorFactory != null && this.advisorFactory.isAspect(beanClass));
    }

}
