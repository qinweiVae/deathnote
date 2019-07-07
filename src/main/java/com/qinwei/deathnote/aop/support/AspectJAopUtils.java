package com.qinwei.deathnote.aop.support;

import com.qinwei.deathnote.aop.aspectj.Advice;
import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.aspectj.AfterAdvice;
import com.qinwei.deathnote.aop.aspectj.BeforeAdvice;
import com.qinwei.deathnote.aop.aspectj.advice.AspectJPrecedenceInformation;

/**
 * @author qinwei
 * @date 2019-07-07
 */
public class AspectJAopUtils {

    public static AspectJPrecedenceInformation getAspectJPrecedenceInformationFor(Advisor anAdvisor) {
        if (anAdvisor instanceof AspectJPrecedenceInformation) {
            return (AspectJPrecedenceInformation) anAdvisor;
        }
        Advice advice = anAdvisor.getAdvice();
        if (advice instanceof AspectJPrecedenceInformation) {
            return (AspectJPrecedenceInformation) advice;
        }
        return null;
    }

    public static boolean isBeforeAdvice(Advisor anAdvisor) {
        AspectJPrecedenceInformation precedenceInfo = getAspectJPrecedenceInformationFor(anAdvisor);
        if (precedenceInfo != null) {
            return precedenceInfo.isBeforeAdvice();
        }
        return (anAdvisor.getAdvice() instanceof BeforeAdvice);
    }

    public static boolean isAfterAdvice(Advisor anAdvisor) {
        AspectJPrecedenceInformation precedenceInfo = getAspectJPrecedenceInformationFor(anAdvisor);
        if (precedenceInfo != null) {
            return precedenceInfo.isAfterAdvice();
        }
        return (anAdvisor.getAdvice() instanceof AfterAdvice);
    }
}
