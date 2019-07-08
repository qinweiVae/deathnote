package com.qinwei.deathnote.aop.support;

import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.aspectj.advice.AspectJPrecedenceInformation;
import com.qinwei.deathnote.context.annotation.AnnotationOrderComparator;

import java.util.Comparator;

/**
 * @author qinwei
 * @date 2019-07-05
 */
public class AspectJPrecedenceComparator implements Comparator<Advisor> {

    private final Comparator<? super Advisor> advisorComparator;

    private static final int HIGHER_PRECEDENCE = -1;

    private static final int SAME_PRECEDENCE = 0;

    private static final int LOWER_PRECEDENCE = 1;

    public AspectJPrecedenceComparator() {
        this.advisorComparator = AnnotationOrderComparator.INSTANCE;
    }

    @Override
    public int compare(Advisor o1, Advisor o2) {
        //先根据@Order 的序号比较
        int advisorPrecedence = this.advisorComparator.compare(o1, o2);
        //如果返回的优先级相同，则按另一种规则比较
        if (advisorPrecedence == SAME_PRECEDENCE && declaredInSameAspect(o1, o2)) {
            advisorPrecedence = comparePrecedenceWithinAspect(o1, o2);
        }
        return advisorPrecedence;
    }

    private int comparePrecedenceWithinAspect(Advisor advisor1, Advisor advisor2) {
        //@After，@AfterReturning，@AfterThrowing这三个注解是afterAdvice
        boolean isAfterAdvice = AspectJAopUtils.isAfterAdvice(advisor1) || AspectJAopUtils.isAfterAdvice(advisor2);

        int adviceDeclarationOrderDelta = getAspectDeclarationOrder(advisor1) - getAspectDeclarationOrder(advisor2);
        // after类型的advice order越大优先级越高，其他的是order越小，优先级越高
        if (isAfterAdvice) {
            if (adviceDeclarationOrderDelta < 0) {
                return LOWER_PRECEDENCE;
            } else if (adviceDeclarationOrderDelta == 0) {
                return SAME_PRECEDENCE;
            } else {
                return HIGHER_PRECEDENCE;
            }
        } else {
            if (adviceDeclarationOrderDelta < 0) {
                return HIGHER_PRECEDENCE;
            } else if (adviceDeclarationOrderDelta == 0) {
                return SAME_PRECEDENCE;
            } else {
                return LOWER_PRECEDENCE;
            }
        }
    }

    /**
     * 比较 bean name 是否 相同
     */
    private boolean declaredInSameAspect(Advisor advisor1, Advisor advisor2) {
        return (hasAspectName(advisor1) && hasAspectName(advisor2) &&
                getAspectName(advisor1).equals(getAspectName(advisor2)));
    }

    private boolean hasAspectName(Advisor anAdvisor) {
        return (anAdvisor instanceof AspectJPrecedenceInformation ||
                anAdvisor.getAdvice() instanceof AspectJPrecedenceInformation);
    }

    private String getAspectName(Advisor anAdvisor) {
        AspectJPrecedenceInformation pi = AspectJAopUtils.getAspectJPrecedenceInformationFor(anAdvisor);
        return pi.getAspectName();
    }

    private int getAspectDeclarationOrder(Advisor anAdvisor) {
        AspectJPrecedenceInformation precedenceInfo = AspectJAopUtils.getAspectJPrecedenceInformationFor(anAdvisor);
        if (precedenceInfo != null) {
            return precedenceInfo.getDeclarationOrder();
        } else {
            return 0;
        }
    }
}
