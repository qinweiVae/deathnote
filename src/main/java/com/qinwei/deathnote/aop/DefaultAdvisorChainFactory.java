package com.qinwei.deathnote.aop;

import com.qinwei.deathnote.aop.adapter.AdvisorAdapterRegistry;
import com.qinwei.deathnote.aop.adapter.GlobalAdvisorAdapterRegistry;
import com.qinwei.deathnote.aop.aspectj.Advisor;
import com.qinwei.deathnote.aop.aspectj.PointcutAdvisor;
import com.qinwei.deathnote.aop.intercept.Interceptor;
import com.qinwei.deathnote.aop.intercept.InterceptorAndDynamicMethodMatcher;
import com.qinwei.deathnote.aop.intercept.MethodInterceptor;
import com.qinwei.deathnote.aop.support.MethodMatcher;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author qinwei
 * @date 2019-06-28
 */
public class DefaultAdvisorChainFactory implements AdvisorChainFactory {

    @Override
    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Advised config, Method method, Class<?> targetClass) {
        AdvisorAdapterRegistry registry = GlobalAdvisorAdapterRegistry.getInstance();
        Advisor[] advisors = config.getAdvisors();

        List<Object> interceptorList = new ArrayList<>(advisors.length);

        Class<?> actualClass = (targetClass != null ? targetClass : method.getDeclaringClass());
        // 遍历Advisor
        for (Advisor advisor : advisors) {
            // 如果该adivosr是PointcutAdvisor类型的
            if (advisor instanceof PointcutAdvisor) {
                PointcutAdvisor pointcutAdvisor = (PointcutAdvisor) advisor;
                //先对类进行过滤匹配,ClassFilter指的@Aspect注解中使用的切点表达式
                if (config.isPreFiltered() || pointcutAdvisor.getPointcut().getClassFilter().matches(actualClass)) {
                    //再对方法进行过滤匹配，MethodMatcher主要指的是@Before，@After等注解中使用的切点表达式
                    MethodMatcher methodMatcher = pointcutAdvisor.getPointcut().getMethodMatcher();
                    boolean match = methodMatcher.matches(method, actualClass);
                    // 如果 方法也匹配了
                    if (match) {
                        MethodInterceptor[] interceptors = registry.getInterceptors(advisor);
                        //MethodMatcher在运行时是否需要做一些检测
                        if (methodMatcher.isRuntime()) {
                            for (MethodInterceptor interceptor : interceptors) {
                                interceptorList.add(new InterceptorAndDynamicMethodMatcher(interceptor, methodMatcher));
                            }
                        } else {
                            interceptorList.addAll(Arrays.asList(interceptors));
                        }
                    }
                }
            }
            //如果 Advisor既不是PointcutAdvisor类型则不用匹配，直接生成拦截器，加入到返回列表
            else {
                Interceptor[] interceptors = registry.getInterceptors(advisor);
                interceptorList.addAll(Arrays.asList(interceptors));
            }
        }
        return interceptorList;
    }

}
