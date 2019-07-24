package com.qinwei.deathnote.context.aop;

import com.qinwei.deathnote.context.annotation.Component;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * @author qinwei
 * @date 2019-07-24
 * todo 利用 aspectj 拦截的bean 不能再设置 @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
 */
@Component
@Aspect
@Slf4j
public class AspectJTest {

    @Before("execution(* com.qinwei.deathnote.context.aop.Man.work())")
    public void before() {
        log.info("before aop .....");
    }

    @After("execution(* com.qinwei.deathnote.context.aop.Man.work())")
    public void after() {
        log.info("after aop .....");
    }

    @AfterReturning(value = "execution(* com.qinwei.deathnote.context.aop.Man.work())", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        log.info("afterReturning aop ..... {}", result);
    }

    @AfterThrowing(value = "execution(* com.qinwei.deathnote.context.aop.Man.work())", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Throwable ex) {
        log.info("afterThrowing aop ..... ex: {}", ex.getMessage());
    }

    @Around("execution(* com.qinwei.deathnote.context.aop.Man.work())")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("enter aop .....");
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("error aop ...", throwable);
        }
        log.info("exit aop .....");
        return result;
    }
}
