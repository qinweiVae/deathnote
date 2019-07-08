package com.qinwei.deathnote.aop.aspectj;

import com.qinwei.deathnote.aop.support.AopUtils;
import com.qinwei.deathnote.aop.support.ClassFilter;
import com.qinwei.deathnote.aop.support.MethodMatcher;
import com.qinwei.deathnote.utils.ClassUtils;
import com.qinwei.deathnote.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        obtainPointcutExpression();

        ShadowMatch shadowMatch = getTargetShadowMatch(method, targetClass);
        //todo
        return false;
    }

    private ShadowMatch getTargetShadowMatch(Method method, Class<?> targetClass) {
        // 拿到实际的 method，比如如果method是接口方法，那么就找到该接口方法的实现类的方法
        Method targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        if (targetMethod.getDeclaringClass().isInterface()) {
            Set<Class<?>> interfaces = ClassUtils.getAllInterfacesAsSet(targetClass);
            if (interfaces.size() > 1) {
                //创建jdk 代理
                Class<?> proxy = ClassUtils.createJdkProxy(interfaces.toArray(new Class[0]), targetClass.getClassLoader());
                targetMethod = ClassUtils.getMostSpecificMethod(targetMethod, proxy);
            }
        }
        return getShadowMatch(targetMethod, method);
    }

    /**
     * double check 设置缓存
     */
    private ShadowMatch getShadowMatch(Method targetMethod, Method originalMethod) {
        ShadowMatch shadowMatch = this.shadowMatchCache.get(targetMethod);
        if (shadowMatch == null) {
            synchronized (this.shadowMatchCache) {
                shadowMatch = this.shadowMatchCache.get(targetMethod);
                if (shadowMatch == null) {
                    shadowMatch = obtainPointcutExpression().matchesMethodExecution(targetMethod);
                    // targetMethod 没有匹配到，则使用 originalMethod
                    if (targetMethod != originalMethod && (shadowMatch == null ||
                            (shadowMatch.neverMatches() && Proxy.isProxyClass(targetMethod.getDeclaringClass())))) {
                        shadowMatch = obtainPointcutExpression().matchesMethodExecution(originalMethod);

                    }
                    // 这里如果目标方法和原始方法都无法与切点表达式匹配，就直接封装一个不匹配的结果
                    if (shadowMatch == null) {
                        shadowMatch = new ShadowMatchImpl(org.aspectj.util.FuzzyBoolean.NO, null, null, null);
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
        return false;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

}
