package com.qinwei.deathnote.aop.aspectj.advice;

import com.qinwei.deathnote.aop.AspectInstanceFactory;
import com.qinwei.deathnote.aop.aspectj.Advice;
import com.qinwei.deathnote.aop.aspectj.AspectJExpressionPointcut;
import com.qinwei.deathnote.aop.aspectj.MethodInvocationProceedingJoinPoint;
import com.qinwei.deathnote.aop.intercept.ExposeInvocationInterceptor;
import com.qinwei.deathnote.aop.intercept.MethodInvocation;
import com.qinwei.deathnote.context.support.parameterdiscover.DefaultParameterNameDiscoverer;
import com.qinwei.deathnote.context.support.parameterdiscover.ParameterNameDiscoverer;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.CollectionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutParameter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-07-02
 */
public abstract class AbstractAspectJAdvice implements Advice, AspectJPrecedenceInformation {

    protected static final String JOIN_POINT_KEY = JoinPoint.class.getName();

    protected transient Method aspectJAdviceMethod;

    private final Class<?>[] parameterTypes;

    // 切点
    private final AspectJExpressionPointcut pointcut;

    private final AspectInstanceFactory aspectInstanceFactory;

    // 就是配置了@Aspect 注解 的 bean name
    private String aspectName = "";

    // 切面顺序
    private int declarationOrder;

    private boolean argumentsIntrospected = false;

    private String[] argumentNames;

    private String returningName;

    private String throwingName;

    private Class<?> returningType = Object.class;

    private Class<?> throwingType = Object.class;

    /**
     * JoinPoint 在 参数中的 索引位置，只支持 0
     */
    private int joinPointArgumentIndex = -1;

    /**
     * 用于快速获取returning ，throwing 参数所在下标，这样就不用每次遍历去取了
     */
    private Map<String, Integer> argumentBindings;

    public AbstractAspectJAdvice(Method aspectJAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aspectInstanceFactory) {
        this.aspectJAdviceMethod = aspectJAdviceMethod;
        this.parameterTypes = aspectJAdviceMethod.getParameterTypes();
        this.pointcut = pointcut;
        this.aspectInstanceFactory = aspectInstanceFactory;
    }

    public final synchronized void calculateArgumentBindings() {
        if (this.argumentsIntrospected || this.parameterTypes.length == 0) {
            return;
        }
        int numUnboundArgs = this.parameterTypes.length;
        // 方法参数 中如果有 JoinPoint 或者 ProceedingJoinPoint，那么只能在第一个位置
        Class<?> parameterType = this.parameterTypes[0];
        if (maybeBindJoinPoint(parameterType) || maybeBindProceedingJoinPoint(parameterType)) {
            numUnboundArgs--;
        }
        if (numUnboundArgs > 0) {
            bindArgumentsByName(numUnboundArgs);
        }
        this.argumentsIntrospected = true;
    }

    private void bindArgumentsByName(int numArgumentsExpectingToBind) {
        if (this.argumentNames == null) {
            this.argumentNames = createParameterNameDiscoverer().getParameterNames(this.aspectJAdviceMethod);
        }
        if (this.argumentNames != null) {
            bindExplicitArguments(numArgumentsExpectingToBind);
        } else {
            throw new IllegalStateException("Advice method [" + this.aspectJAdviceMethod.getName() + "] " +
                    "requires " + numArgumentsExpectingToBind + " arguments to be bound by name, but " +
                    "the argument names were not specified and could not be discovered.");
        }
    }

    private void bindExplicitArguments(int numArgumentsLeftToBind) {
        this.argumentBindings = new HashMap<>();
        // 如果参数个数不同
        int numExpectedArgumentNames = this.aspectJAdviceMethod.getParameterCount();
        if (this.argumentNames.length != numExpectedArgumentNames) {
            throw new IllegalStateException("Expecting to find " + numExpectedArgumentNames + " arguments to bind by name in advice, but actually found " + this.argumentNames.length + " arguments.");
        }
        // 方法参数 中如果有 JoinPoint 或者 ProceedingJoinPoint，那么只能在第一个位置
        // 所以这里放入map中的是除JoinPoint 或者 ProceedingJoinPoint 之外的参数
        int argumentIndexOffset = this.parameterTypes.length - numArgumentsLeftToBind;
        for (int i = argumentIndexOffset; i < this.argumentNames.length; i++) {
            this.argumentBindings.put(this.argumentNames[i], i);
        }
        // 如果 是 @AfterReturning
        if (this.returningName != null) {
            if (!this.argumentBindings.containsKey(this.returningName)) {
                throw new IllegalStateException("Returning argument name '" + this.returningName + "' was not bound in advice arguments");
            }
            Integer index = this.argumentBindings.get(this.returningName);
            this.returningType = this.aspectJAdviceMethod.getParameterTypes()[index];
        }
        // 如果是 @AfterThrowing
        if (this.throwingName != null) {
            if (!this.argumentBindings.containsKey(this.throwingName)) {
                throw new IllegalStateException("Throwing argument name '" + this.throwingName + "' was not bound in advice arguments");
            }
            Integer index = this.argumentBindings.get(this.throwingName);
            this.throwingType = this.aspectJAdviceMethod.getParameterTypes()[index];
        }
    }

    private ParameterNameDiscoverer createParameterNameDiscoverer() {
        ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
        return parameterNameDiscoverer;
    }

    private boolean maybeBindJoinPoint(Class<?> candidateParameterType) {
        if (JoinPoint.class == candidateParameterType) {
            this.joinPointArgumentIndex = 0;
            return true;
        }
        return false;
    }

    private boolean maybeBindProceedingJoinPoint(Class<?> candidateParameterType) {
        if (ProceedingJoinPoint.class == candidateParameterType) {
            // 只有 @Around 才支持 ProceedingJoinPoint
            if (!supportsProceedingJoinPoint()) {
                throw new IllegalArgumentException("ProceedingJoinPoint is only supported for around advice");
            }
            this.joinPointArgumentIndex = 0;
            return true;
        }
        return false;
    }

    protected JoinPointMatch getJoinPointMatch() {
        MethodInvocation methodInvocation = ExposeInvocationInterceptor.currentInvocation();
        return getJoinPointMatch(methodInvocation);
    }

    protected JoinPointMatch getJoinPointMatch(MethodInvocation methodInvocation) {
        String expression = this.pointcut.getExpression();
        return (expression != null ? (JoinPointMatch) methodInvocation.getUserAttribute(expression) : null);
    }

    protected Object invokeAdviceMethod(JoinPointMatch jpMatch, Object returnValue, Throwable ex) throws Throwable {

        return invokeAdviceMethodWithGivenArgs(argBinding(getJoinPoint(), jpMatch, returnValue, ex));
    }

    protected Object invokeAdviceMethod(JoinPoint jp, JoinPointMatch jpMatch, Object returnValue, Throwable t) throws Throwable {

        return invokeAdviceMethodWithGivenArgs(argBinding(jp, jpMatch, returnValue, t));
    }

    /**
     * 反射执行 method
     */
    protected Object invokeAdviceMethodWithGivenArgs(Object[] args) throws Throwable {
        Object[] actualArgs = args;
        if (this.aspectJAdviceMethod.getParameterCount() == 0) {
            actualArgs = null;
        }
        ClassUtils.makeAccessible(this.aspectJAdviceMethod);
        return this.aspectJAdviceMethod.invoke(this.aspectInstanceFactory.getAspectInstance(), actualArgs);
    }

    /**
     * 参数配置
     */
    protected Object[] argBinding(JoinPoint jp, JoinPointMatch jpMatch, Object returnValue, Throwable ex) {
        calculateArgumentBindings();

        Object[] adviceInvocationArgs = new Object[this.parameterTypes.length];
        int numBound = 0;
        // 方法参数 中如果有 JoinPoint 或者 ProceedingJoinPoint，那么只能在第一个位置
        if (this.joinPointArgumentIndex != -1) {
            adviceInvocationArgs[0] = jp;
            numBound++;
        }
        //还存在其他参数(只支持 @AfterThrowing 的 throwing 和 @AfterReturning 的 returning)
        if (CollectionUtils.isNotEmpty(this.argumentBindings)) {
            // 绑定 pointcut
            if (jpMatch != null) {
                PointcutParameter[] parameterBindings = jpMatch.getParameterBindings();
                for (PointcutParameter parameter : parameterBindings) {
                    String name = parameter.getName();
                    Integer index = this.argumentBindings.get(name);
                    adviceInvocationArgs[index] = parameter.getBinding();
                    numBound++;
                }
            }
            //绑定 returning
            if (this.returningName != null) {
                Integer index = this.argumentBindings.get(this.returningName);
                adviceInvocationArgs[index] = returnValue;
                numBound++;
            }
            //绑定 throwing
            if (this.throwingName != null) {
                Integer index = this.argumentBindings.get(this.throwingName);
                adviceInvocationArgs[index] = ex;
                numBound++;
            }
        }
        // 如果还有其他参数，直接抛异常
        if (numBound != this.parameterTypes.length) {
            throw new IllegalStateException("Required to bind " + this.parameterTypes.length + " arguments, but only bound " + numBound + " (JoinPointMatch " + (jpMatch == null ? "was NOT" : "WAS") + " bound in invocation)");
        }
        return adviceInvocationArgs;
    }

    /**
     * 从 threadlocal 中 拿到 JoinPoint
     */
    protected JoinPoint getJoinPoint() {
        MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
        JoinPoint jp = (JoinPoint) mi.getUserAttribute(JOIN_POINT_KEY);
        if (jp == null) {
            jp = new MethodInvocationProceedingJoinPoint(mi);
            mi.setUserAttribute(JOIN_POINT_KEY, jp);
        }
        return jp;
    }

    protected boolean supportsProceedingJoinPoint() {
        return false;
    }

    public void setAspectName(String name) {
        this.aspectName = name;
    }

    /**
     * 就是配置了@Aspect 注解 的 bean name
     */
    @Override
    public String getAspectName() {
        return this.aspectName;
    }

    public void setDeclarationOrder(int order) {
        this.declarationOrder = order;
    }

    @Override
    public int getDeclarationOrder() {
        return this.declarationOrder;
    }

    protected Class<?> getThrowingType() {
        return this.throwingType;
    }

    protected Class<?> getReturningType() {
        return this.returningType;
    }

    public void setReturningName(String name) {
        throw new UnsupportedOperationException("Only afterReturning advice can be used to bind a return value");
    }

    public void setThrowingName(String name) {
        throw new UnsupportedOperationException("Only afterThrowing advice can be used to bind a thrown exception");
    }

    protected void setReturningNameNoCheck(String name) {
        this.returningName = name;
    }

    protected void setThrowingNameNoCheck(String name) {
        this.throwingName = name;
    }
}
