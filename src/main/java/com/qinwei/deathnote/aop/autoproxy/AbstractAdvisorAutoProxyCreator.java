package com.qinwei.deathnote.aop.autoproxy;

import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.support.AopUtils;
import com.qinwei.deathnote.context.annotation.AnnotationOrderComparator;
import com.qinwei.deathnote.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public abstract class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {

    @Override
    protected Advisor[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName) {
        //查找合适的通知器
        List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
        if (advisors.isEmpty()) {
            return null;
        }
        return advisors.toArray(new Advisor[0]);
    }

    /**
     * 查找合适的通知器
     */
    protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
        //查找所有的通知器
        List<Advisor> candidateAdvisors = findCandidateAdvisors();
        //筛选可应用在 beanClass 上的 Advisor，通过 ClassFilter 和 MethodMatcher对目标类和方法进行匹配
        List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
        if (CollectionUtils.isEmpty(eligibleAdvisors)) {
            AnnotationOrderComparator.sort(eligibleAdvisors);
        }
        return eligibleAdvisors;
    }

    /**
     * 筛选可应用在 beanClass 上的 Advisor，通过 ClassFilter 和 MethodMatcher对目标类和方法进行匹配
     */
    protected List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> beanClass, String beanName) {
        return AopUtils.findAdvisorsThatCanApply(candidateAdvisors, beanClass);
    }

    /**
     * 查找所有的通知器
     */
    protected List<Advisor> findCandidateAdvisors() {
        String[] advisorNames = getBeanFactory().getBeanNamesForType(Advisor.class);
        if (advisorNames.length == 0) {
            return new ArrayList<>();
        }
        List<Advisor> advisors = new ArrayList<>();
        for (String name : advisorNames) {
            advisors.add(getBeanFactory().getBean(name, Advisor.class));
        }
        return advisors;
    }
}
