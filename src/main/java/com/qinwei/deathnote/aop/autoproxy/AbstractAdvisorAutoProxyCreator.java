package com.qinwei.deathnote.aop.autoproxy;

import com.qinwei.deathnote.aop.annotation.PointcutAdvisorImpl;
import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.aspectj.AspectJExpressionPointcut;
import com.qinwei.deathnote.aop.aspectj.PointcutAdvisor;
import com.qinwei.deathnote.aop.aspectj.advice.AbstractAspectJAdvice;
import com.qinwei.deathnote.aop.intercept.ExposeInvocationInterceptor;
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
        List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass);

        makeAdvisorChainAspectJCapableIfNecessary(eligibleAdvisors);

        if (CollectionUtils.isNotEmpty(eligibleAdvisors)) {
            sortAdvisors(eligibleAdvisors);
        }
        return eligibleAdvisors;
    }

    private void makeAdvisorChainAspectJCapableIfNecessary(List<Advisor> advisors) {
        // 如果通知器列表是一个空列表，则啥都不做
        if (!advisors.isEmpty()) {
            boolean foundAspectJAdvice = false;
            //循环用于检测 advisors 列表中是否存在 AspectJ 类型的 Advisor 或 Advice
            for (Advisor advisor : advisors) {
                if (isAspectJAdvice(advisor)) {
                    foundAspectJAdvice = true;
                    break;
                }
            }
            //向 advisors 列表的首部添加 DefaultPointcutAdvisor，即 ExposeInvocationInterceptor
            if (foundAspectJAdvice && !advisors.contains(ExposeInvocationInterceptor.ADVISOR)) {
                advisors.add(0, ExposeInvocationInterceptor.ADVISOR);
            }
        }
    }

    private boolean isAspectJAdvice(Advisor advisor) {
        return (advisor instanceof PointcutAdvisorImpl ||
                advisor.getAdvice() instanceof AbstractAspectJAdvice ||
                (advisor instanceof PointcutAdvisor &&
                        ((PointcutAdvisor) advisor).getPointcut() instanceof AspectJExpressionPointcut));
    }

    protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
        AnnotationOrderComparator.sort(advisors);
        return advisors;
    }

    /**
     * 筛选可应用在 beanClass 上的 Advisor，通过 ClassFilter 和 MethodMatcher对目标类和方法进行匹配
     */
    protected List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> beanClass) {
        return AopUtils.findAdvisorsThatCanApply(candidateAdvisors, beanClass);
    }

    /**
     * 从 beanFactory 里面查找所有 Advisor 的bean
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

    @Override
    protected boolean advisorsPreFiltered() {
        return true;
    }
}
