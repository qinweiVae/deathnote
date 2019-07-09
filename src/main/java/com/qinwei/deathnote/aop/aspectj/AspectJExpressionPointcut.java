package com.qinwei.deathnote.aop.aspectj;

import com.qinwei.deathnote.aop.intercept.ExposeInvocationInterceptor;
import com.qinwei.deathnote.aop.intercept.MethodInvocation;
import com.qinwei.deathnote.aop.support.AopUtils;
import com.qinwei.deathnote.aop.support.ClassFilter;
import com.qinwei.deathnote.aop.support.MethodMatcher;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.reflect.ShadowMatchImpl;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.aspectj.weaver.tools.ShadowMatch;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinwei
 * @date 2019-07-02
 */
@Slf4j
public class AspectJExpressionPointcut implements ClassFilter, MethodMatcher, Pointcut {

    private static final Set<PointcutPrimitive> SUPPORTED_PRIMITIVES = new HashSet<>();

    static {
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.EXECUTION);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.ARGS);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.REFERENCE);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.THIS);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.TARGET);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.WITHIN);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ANNOTATION);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_WITHIN);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ARGS);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_TARGET);
    }

    private String expression;

    private Class<?> pointcutDeclarationScope;

    //用来判断方法或者类是否匹配表达式
    private transient PointcutExpression pointcutExpression;

    private transient Map<Method, ShadowMatch> shadowMatchCache = new ConcurrentHashMap<>(32);

    public AspectJExpressionPointcut(Class<?> declarationScope) {
        this.pointcutDeclarationScope = declarationScope;
    }

    @Override
    public ClassFilter getClassFilter() {
        obtainPointcutExpression();
        return this;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        obtainPointcutExpression();
        return this;
    }

    private PointcutExpression obtainPointcutExpression() {
        if (getExpression() == null) {
            throw new IllegalStateException("Must set property 'expression' before attempting to match");
        }
        if (this.pointcutExpression == null) {
            this.pointcutExpression = buildPointcutExpression();
        }
        return this.pointcutExpression;
    }

    private PointcutExpression buildPointcutExpression() {
        PointcutParser parser = PointcutParser.
                getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(SUPPORTED_PRIMITIVES, ClassUtils.getDefaultClassLoader());
        return parser.parsePointcutExpression(replaceBooleanOperators(getExpression()), this.pointcutDeclarationScope, new PointcutParameter[0]);
    }

    /**
     * 可以支持 and , or , not
     */
    private String replaceBooleanOperators(String pcExpr) {
        if (pcExpr == null) {
            throw new IllegalArgumentException("No expression set");
        }
        String result = StringUtils.replace(pcExpr, " and ", " && ");
        result = StringUtils.replace(result, " or ", " || ");
        result = StringUtils.replace(result, " not ", " ! ");
        return result;
    }

    @Override
    public boolean matches(Class<?> clazz) {
        PointcutExpression pointcutExpression = obtainPointcutExpression();
        try {
            return pointcutExpression.couldMatchJoinPointsInType(clazz);
        } catch (Exception e) {
            log.warn("PointcutExpression matching rejected target class", e);
        }
        return false;
    }

    /**
     * 1.从缓存中获取ShadowMatch数据，如果缓存中存在则直接返回
     * 2.如果缓存中不存在，解析切点表达式与当前方法进行匹配，将匹配后的结果封装成ShadowMatch返回。如果匹配失败，则在目标方法上找切点表达式，组装成为一个回调切点表达式，并且对回调切点表达式进行解析，使用回调切点表达与目标方法进行匹配。
     * 3.如果目标方法与当前切点表达式匹配失败，则用其原始方法与切点表达式匹配。如果匹配失败，则在目标方法上找切点表达式，组装成为一个回调切点表达式，并且对回调切点表达式进行解析，使用回调切点表达与目标方法进行匹配。
     * 4. 如果目标方法和原始方法都与切点表达式匹配失败，就封装一个不匹配的结果。
     * 5.如果通过匹配结果无法判断，就将匹配得到的ShadowMatch和回调的ShadowMatch封装到DefensiveShadowMatch中。
     * 6.将匹配结果缓存起来。
     */
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        obtainPointcutExpression();

        ShadowMatch shadowMatch = getTargetShadowMatch(method, targetClass);
        if (shadowMatch.alwaysMatches()) {
            return true;
        }
        if (shadowMatch.neverMatches()) {
            return false;
        }
        //如果不确认能否匹配，则将匹配结果封装为一个RuntimeTestWalker，在方法运行时可进行动态匹配
        return false;
    }

    private ShadowMatch getTargetShadowMatch(Method method, Class<?> targetClass) {
        // 拿到实际的 method (被重写的方法、接口类的实现方法、桥接方法)，比如如果method是接口方法，那么就找到该接口方法的实现类的方法
        Method targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        if (targetMethod.getDeclaringClass().isInterface()) {
            Set<Class<?>> interfaces = ClassUtils.getAllInterfacesAsSet(targetClass);
            if (interfaces.size() > 1) {
                //创建jdk 代理
                Class<?> proxy = ClassUtils.createJdkProxy(interfaces.toArray(new Class[0]), targetClass.getClassLoader());
                targetMethod = ClassUtils.getMostSpecificMethod(targetMethod, proxy);
            }
        }
        // 将对切点表达式解析后的结果与匹配的目标方法封装为一个ShadowMatch对象，并且对目标方法进行匹配，匹配的结果将存储在ShadowMatch.match参数中
        return getShadowMatch(targetMethod, method);
    }

    /**
     * double check 设置缓存
     * <p>
     * 将对切点表达式解析后的结果与匹配的目标方法封装为一个ShadowMatch对象，并且对目标方法进行匹配，匹配的结果将存储在ShadowMatch.match参数中
     */
    private ShadowMatch getShadowMatch(Method targetMethod, Method originalMethod) {
        // 从缓存中获取ShadowMatch数据，如果缓存中存在则直接返回
        ShadowMatch shadowMatch = this.shadowMatchCache.get(targetMethod);
        if (shadowMatch == null) {
            synchronized (this.shadowMatchCache) {
                shadowMatch = this.shadowMatchCache.get(targetMethod);
                if (shadowMatch == null) {
                    // 解析切点表达式与当前方法进行匹配，将匹配后的结果封装成ShadowMatch返回
                    shadowMatch = obtainPointcutExpression().matchesMethodExecution(targetMethod);

                    if (targetMethod != originalMethod &&
                            (shadowMatch == null ||
                                    (shadowMatch.neverMatches() && Proxy.isProxyClass(targetMethod.getDeclaringClass())))) {
                        // 如果目标方法与当前切点表达式匹配失败，则用其原始方法与切点表达式匹配
                        shadowMatch = obtainPointcutExpression().matchesMethodExecution(originalMethod);
                    }
                    // 这里如果目标方法和原始方法都无法与切点表达式匹配，就直接封装一个不匹配的结果
                    if (shadowMatch == null) {
                        shadowMatch = new ShadowMatchImpl(FuzzyBoolean.NO, null, null, null);
                    }
                    this.shadowMatchCache.put(targetMethod, shadowMatch);
                }
            }
        }
        return shadowMatch;
    }

    @Override
    public boolean isRuntime() {
        return obtainPointcutExpression().mayNeedDynamicTest();
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass, Object... args) {
        obtainPointcutExpression();

        ShadowMatch shadowMatch = getTargetShadowMatch(method, targetClass);

        MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
        Object targetObject = mi.getThis();
        Object thisObject = mi.getProxy();
        //todo
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

}
