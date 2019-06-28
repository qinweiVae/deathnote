package com.qinwei.deathnote.aop;

import com.qinwei.deathnote.aop.aspectj.Advice;
import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.target.TargetClassAware;
import com.qinwei.deathnote.aop.target.TargetSource;

/**
 * @author qinwei
 * @date 2019-06-27
 */
public interface Advised extends TargetClassAware {

    Class<?>[] getProxiedInterfaces();

    boolean isInterfaceProxied(Class<?> intf);

    void setTargetSource(TargetSource targetSource);

    TargetSource getTargetSource();

    void setPreFiltered(boolean preFiltered);

    boolean isPreFiltered();

    Advisor[] getAdvisors();

    void addAdvisor(Advisor advisor) ;

    void addAdvisor(int pos, Advisor advisor);

    boolean removeAdvisor(Advisor advisor);

    void removeAdvisor(int index) ;

    int indexOf(Advisor advisor);

    void addAdvice(Advice advice);

    void addAdvice(int pos, Advice advice);

    boolean removeAdvice(Advice advice);

    int indexOf(Advice advice);
}
