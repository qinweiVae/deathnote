package com.qinwei.deathnote.aop.autoproxy;

import com.qinwei.deathnote.aop.Advised;
import com.qinwei.deathnote.aop.intercept.ReflectiveMethodInvocation;
import com.qinwei.deathnote.aop.support.AdvisedSupport;
import com.qinwei.deathnote.aop.support.AopContext;
import com.qinwei.deathnote.aop.support.AopUtils;
import com.qinwei.deathnote.aop.targetSource.EmptyTargetSource;
import com.qinwei.deathnote.aop.targetSource.TargetSource;
import com.qinwei.deathnote.utils.ClassUtils;
import com.sun.istack.internal.Nullable;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.core.ClassGenerator;
import net.sf.cglib.core.CodeGenerationException;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.transform.impl.UndeclaredThrowableStrategy;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * @author qinwei
 * @date 2019-07-01
 */
@Slf4j
class CglibAopProxy implements AopProxy, Serializable {

    private static final long serialVersionUID = -6334798172597262590L;

    /**
     * 在CglibAopProxy内部定义了一组常量用于表示生成的callback索引。
     * <p>
     * AOP_PROXY = 0       一般的aop调用
     * <p>
     * INVOKE_TARGET = 1   直接调用目标方法
     * <p>
     * NO_OVERRIDE = 2     不能覆盖的方法，比如finalize方法。
     * <p>
     * DISPATCH_TARGET = 3 直接对原来的bean进行方法调用。
     * <p>
     * DISPATCH_ADVISED = 4 对Advised接口方法有效
     * <p>
     * INVOKE_EQUALS = 5    对equals方法拦截
     * <p>
     * INVOKE_HASHCODE = 6  对hashCode方法拦截
     */
    private static final int AOP_PROXY = 0;
    private static final int INVOKE_TARGET = 1;
    private static final int NO_OVERRIDE = 2;
    private static final int DISPATCH_TARGET = 3;
    private static final int DISPATCH_ADVISED = 4;
    private static final int INVOKE_EQUALS = 5;
    private static final int INVOKE_HASHCODE = 6;

    private static final Map<Class<?>, Boolean> validatedClasses = new WeakHashMap<>();

    private final AdvisedSupport advised;

    private final transient AdvisedDispatcher advisedDispatcher;

    public CglibAopProxy(AdvisedSupport config) {
        assert config != null : "AdvisedSupport must not be null";
        if (config.getAdvisors().length == 0 && config.getTargetSource() == EmptyTargetSource.INSTANCE) {
            throw new IllegalStateException("No advisors and no TargetSource specified");
        }
        this.advised = config;
        this.advisedDispatcher = new AdvisedDispatcher(this.advised);
    }

    @Override
    public Object getProxy() {
        return getProxy(ClassUtils.getDefaultClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        //从advised 中获取目标对象
        Class<?> rootClass = this.advised.getTargetClass();
        if (rootClass == null) {
            throw new IllegalStateException("Target class must be available for creating a CGLIB proxy");
        }
        try {
            Class<?> proxySuperClass = rootClass;
            // 这里判断rootClass是否是Cglib代理所产生的类（判断rootClass的className是否包含$$）
            if (ClassUtils.isCglibProxyClass(rootClass)) {
                // 取目标类的父类作为目标类
                proxySuperClass = rootClass.getSuperclass();
                // 拿到 目标对象实现的 所有接口，并添加到 AdvisedSupport
                Class<?>[] additionalInterfaces = rootClass.getInterfaces();
                for (Class<?> additionalInterface : additionalInterfaces) {
                    this.advised.addInterface(additionalInterface);
                }
            }
            // 验证proxySuperClass中的是否有final方法（有则打印出来警告信息，该方法不能被代理）
            validateClassIfNecessary(proxySuperClass, classLoader);

            // 配置enhancer如代理接口,回调等
            Enhancer enhancer = new Enhancer();
            if (classLoader != null) {
                enhancer.setClassLoader(classLoader);
            }
            enhancer.setSuperclass(proxySuperClass);
            enhancer.setInterfaces(AopUtils.completeProxiedInterfaces(this.advised));
            enhancer.setStrategy(new ClassLoaderAwareUndeclaredThrowableStrategy(classLoader));

            // 获取回调方法，这里是重点
            Callback[] callbacks = getCallbacks(rootClass);

            Class<?>[] types = new Class<?>[callbacks.length];
            for (int x = 0; x < types.length; x++) {
                types[x] = callbacks[x].getClass();
            }

            // callbackFilter的作用主要是建立了method与callback编号的映射
            enhancer.setCallbackFilter(new ProxyCallbackFilter(this.advised.getConfigurationOnlyCopy()));
            enhancer.setCallbackTypes(types);

            // 生成代理的class以及代理bean实例。
            return createProxyClassAndInstance(enhancer, callbacks);

        } catch (CodeGenerationException | IllegalArgumentException ex) {
            throw new IllegalStateException("Could not generate CGLIB subclass of " + this.advised.getTargetClass() +
                    ": Common causes of this problem include using a final class or a non-visible class", ex);
        } catch (Throwable e) {
            throw new RuntimeException("Unexpected AOP exception", e);
        }
    }

    private Object createProxyClassAndInstance(Enhancer enhancer, Callback[] callbacks) {
        Class<?> proxyClass = enhancer.createClass();
        Object proxyInstance;
        try {
            Constructor<?> ctor = proxyClass.getDeclaredConstructor();
            ClassUtils.makeAccessible(ctor);
            proxyInstance = ctor.newInstance();
        } catch (Throwable ex) {
            throw new IllegalStateException("Unable to instantiate proxy using Objenesis, and regular proxy instantiation via default constructor fails as well", ex);
        }
        ((Factory) proxyInstance).setCallbacks(callbacks);
        return proxyInstance;
    }

    private Callback[] getCallbacks(Class<?> rootClass) throws Exception {
        boolean exposeProxy = this.advised.isExposeProxy();
        boolean isStatic = this.advised.getTargetSource().isStatic();

        // 创建callback 这个DynamicAdvisedInterceptor是一个实现了MethodInterceptor接口的类
        Callback aopInterceptor = new DynamicAdvisedInterceptor(this.advised);
        /*
         * 不需要增强,但可能会返回this的方法:
         * 如果targetSource是静态的话(每次getTarget都是同一个对象),使用StaticUnadvisedExposedInterceptor
         * 否则使用DynamicUnadvisedExposedInterceptor。
         */
        Callback targetInterceptor;
        if (exposeProxy) {
            targetInterceptor = isStatic ?
                    new StaticUnadvisedExposedInterceptor(this.advised.getTargetSource().getTarget()) :
                    new DynamicUnadvisedExposedInterceptor(this.advised.getTargetSource());
        } else {
            targetInterceptor = isStatic ?
                    new StaticUnadvisedInterceptor(this.advised.getTargetSource().getTarget()) :
                    new DynamicUnadvisedInterceptor(this.advised.getTargetSource());
        }

        Callback targetDispatcher = isStatic ?
                new StaticDispatcher(this.advised.getTargetSource().getTarget()) : new SerializableNoOp();

        Callback[] mainCallbacks = new Callback[]{
                aopInterceptor,
                targetInterceptor,
                new SerializableNoOp(),
                targetDispatcher,
                this.advisedDispatcher,
                new EqualsInterceptor(this.advised),
                new HashCodeInterceptor(this.advised)
        };

        return mainCallbacks;
    }

    /**
     * 如果返回的是this,则判断是否需要把retVal设置为代理对象。
     */
    private static Object processReturnType(Object proxy, Object target, Method method, Object returnValue) {
        if (returnValue != null && returnValue == target) {
            returnValue = proxy;
        }
        Class<?> returnType = method.getReturnType();
        if (returnValue == null && returnType != Void.TYPE && returnType.isPrimitive()) {
            throw new IllegalStateException("Null return value from advice does not match primitive return type for: " + method);
        }
        return returnValue;
    }

    private void validateClassIfNecessary(Class<?> proxySuperClass, ClassLoader proxyClassLoader) {
        synchronized (validatedClasses) {
            if (!validatedClasses.containsKey(proxySuperClass)) {
                doValidateClass(proxySuperClass, proxyClassLoader, ClassUtils.getAllInterfacesAsSet(proxySuperClass));
                validatedClasses.put(proxySuperClass, Boolean.TRUE);
            }
        }
    }

    private void doValidateClass(Class<?> proxySuperClass, ClassLoader proxyClassLoader, Set<Class<?>> interfaces) {
        if (proxySuperClass != Object.class) {
            Method[] methods = proxySuperClass.getDeclaredMethods();
            for (Method method : methods) {
                int mod = method.getModifiers();
                if (!Modifier.isStatic(mod) && !Modifier.isPrivate(mod)) {
                    if (Modifier.isFinal(mod)) {
                        if (implementsInterface(method, interfaces)) {
                            log.info("Unable to proxy interface-implementing method [" + method + "] because " +
                                    "it is marked as final: Consider using interface-based JDK proxies instead!");
                        }
                        log.info("Final method [" + method + "] cannot get proxied via CGLIB: " +
                                "Calls to this method will NOT be routed to the target instance and " +
                                "might lead to NPEs against uninitialized fields in the proxy instance.");
                    } else if (!Modifier.isPublic(mod) && !Modifier.isProtected(mod) &&
                            proxyClassLoader != null && proxySuperClass.getClassLoader() != proxyClassLoader) {
                        log.info("Method [" + method + "] is package-visible across different ClassLoaders " +
                                "and cannot get proxied via CGLIB: Declare this method as public or protected " +
                                "if you need to support invocations through the proxy.");
                    }
                }
            }
            doValidateClass(proxySuperClass.getSuperclass(), proxyClassLoader, interfaces);
        }
    }

    private static boolean implementsInterface(Method method, Set<Class<?>> ifcs) {
        for (Class<?> ifc : ifcs) {
            if (ClassUtils.hasMethod(ifc, method.getName(), method.getParameterTypes())) {
                return true;
            }
        }
        return false;
    }

    private static class ProxyCallbackFilter implements CallbackFilter {
        private final AdvisedSupport advised;

        public ProxyCallbackFilter(AdvisedSupport advised) {
            this.advised = advised;
        }

        @Override
        public int accept(Method method) {
            // finalize方法使用SerializableNoOp
            if (ClassUtils.isFinalizeMethod(method)) {
                log.debug("Found finalize() method - using NO_OVERRIDE");
                return NO_OVERRIDE;
            }
            // 如果是  Advised 接口
            if (method.getDeclaringClass().isInterface() && method.getDeclaringClass().isAssignableFrom(Advised.class)) {
                log.debug("Method is declared on Advised interface: " + method);
                return DISPATCH_ADVISED;
            }
            // 如果是 equals方法使用EqualsInterceptor
            if (ClassUtils.isEqualsMethod(method)) {
                log.debug("Found 'equals' method: " + method);
                return INVOKE_EQUALS;
            }
            // 如果是 hashCode方法使用HashCodeInterceptor
            if (ClassUtils.isHashCodeMethod(method)) {
                log.debug("Found 'hashCode' method: " + method);
                return INVOKE_HASHCODE;
            }

            Class<?> targetClass = this.advised.getTargetClass();
            List<?> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
            boolean exposeProxy = this.advised.isExposeProxy();
            boolean isStatic = this.advised.getTargetSource().isStatic();
            if (!chain.isEmpty()) {
                return AOP_PROXY;
            } else {
                /*
                 * 如果需要暴露proxy或者targetSource非静态,则使用的callback可能是下面三种之一。
                 * StaticUnadvisedExposedInterceptor
                 * DynamicUnadvisedExposedInterceptor
                 * DynamicUnadvisedInterceptor
                 */
                if (exposeProxy || !isStatic) {
                    return INVOKE_TARGET;
                }
                Class<?> returnType = method.getReturnType();
                // 返回的类型与targetClass一致(有可能返回this),使用StaticUnadvisedInterceptor
                if (targetClass != null && returnType.isAssignableFrom(targetClass)) {
                    log.debug("Method return type is assignable from target type and may therefore return 'this' - using INVOKE_TARGET: " + method);
                    return INVOKE_TARGET;
                } else {
                    log.debug("Method return type ensures 'this' cannot be returned -  using DISPATCH_TARGET: " + method);
                    return DISPATCH_TARGET;
                }
            }
        }
    }

    private static class AdvisedDispatcher implements Dispatcher, Serializable {

        private final AdvisedSupport advised;

        public AdvisedDispatcher(AdvisedSupport advised) {
            this.advised = advised;
        }

        @Override
        public Object loadObject() throws Exception {
            return this.advised;
        }
    }

    private static class EqualsInterceptor implements MethodInterceptor, Serializable {

        private final AdvisedSupport advised;

        public EqualsInterceptor(AdvisedSupport advised) {
            this.advised = advised;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
            Object other = args[0];
            if (proxy == other) {
                return true;
            }
            if (other instanceof Factory) {
                Callback callback = ((Factory) other).getCallback(INVOKE_EQUALS);
                if (!(callback instanceof EqualsInterceptor)) {
                    return false;
                }
                AdvisedSupport otherAdvised = ((EqualsInterceptor) callback).advised;
                return AopUtils.equalsInProxy(this.advised, otherAdvised);
            } else {
                return false;
            }
        }
    }

    private static class HashCodeInterceptor implements MethodInterceptor, Serializable {

        private final AdvisedSupport advised;

        public HashCodeInterceptor(AdvisedSupport advised) {
            this.advised = advised;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
            return CglibAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
        }
    }

    public static class SerializableNoOp implements NoOp, Serializable {
    }

    /**
     * 如果 method 返回的不是 this ，使用 Dispatcher 比 interceptor 更快
     */
    private static class StaticDispatcher implements Dispatcher, Serializable {

        @Nullable
        private Object target;

        public StaticDispatcher(Object target) {
            this.target = target;
        }

        @Override
        public Object loadObject() {
            return this.target;
        }
    }


    private static class DynamicUnadvisedExposedInterceptor implements MethodInterceptor, Serializable {

        private final TargetSource targetSource;

        public DynamicUnadvisedExposedInterceptor(TargetSource targetSource) {
            this.targetSource = targetSource;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object oldProxy = null;
            Object target = this.targetSource.getTarget();
            try {
                oldProxy = AopContext.setCurrentProxy(proxy);
                Object retVal = methodProxy.invoke(target, args);
                return processReturnType(proxy, target, method, retVal);
            } finally {
                AopContext.setCurrentProxy(oldProxy);
                if (target != null) {
                    this.targetSource.releaseTarget(target);
                }
            }
        }
    }


    private static class StaticUnadvisedExposedInterceptor implements MethodInterceptor, Serializable {

        private final Object target;

        public StaticUnadvisedExposedInterceptor(Object target) {
            this.target = target;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object oldProxy = null;
            try {
                oldProxy = AopContext.setCurrentProxy(proxy);
                Object retVal = methodProxy.invoke(this.target, args);
                return processReturnType(proxy, this.target, method, retVal);
            } finally {
                AopContext.setCurrentProxy(oldProxy);
            }
        }
    }

    private static class StaticUnadvisedInterceptor implements MethodInterceptor, Serializable {

        private final Object target;

        public StaticUnadvisedInterceptor(Object target) {
            this.target = target;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object retVal = methodProxy.invoke(this.target, args);
            return processReturnType(proxy, this.target, method, retVal);
        }
    }

    private static class DynamicUnadvisedInterceptor implements MethodInterceptor, Serializable {

        private final TargetSource targetSource;

        public DynamicUnadvisedInterceptor(TargetSource targetSource) {
            this.targetSource = targetSource;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object target = this.targetSource.getTarget();
            try {
                Object retVal = methodProxy.invoke(target, args);
                return processReturnType(proxy, target, method, retVal);
            } finally {
                if (target != null) {
                    this.targetSource.releaseTarget(target);
                }
            }
        }
    }

    private static class DynamicAdvisedInterceptor implements MethodInterceptor, Serializable {

        private final AdvisedSupport advised;

        public DynamicAdvisedInterceptor(AdvisedSupport advised) {
            this.advised = advised;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object oldProxy = null;
            boolean setProxyContext = false;
            Object target = null;
            TargetSource targetSource = this.advised.getTargetSource();

            try {
                // 这里对应的是 expose-proxy 属性的应用，把代理暴露处理
                if (this.advised.isExposeProxy()) {
                    // 向 AopContext 中设置代理对象
                    oldProxy = AopContext.setCurrentProxy(proxy);
                    setProxyContext = true;
                }
                // 拿出原始bean对象
                target = targetSource.getTarget();
                Class<?> targetClass = target != null ? target.getClass() : null;
                // 获取该方法对应的拦截器链。
                List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
                Object retVal;
                // 如果没有增强并且方法为public,则直接调用
                if (chain.isEmpty() && Modifier.isPublic(method.getModifiers())) {
                    //解析method 的可变参数
                    Object[] argsToUse = AopUtils.adaptArgumentsIfNecessary(method, args);
                    retVal = methodProxy.invoke(target, argsToUse);
                } else {
                    // 创建拦截器链并调用
                    retVal = new CglibMethodInvocation(proxy, target, method, args, targetClass, chain, methodProxy).proceed();
                }
                // 如果返回的是this,则判断是否需要把retVal设置为代理对象。
                retVal = processReturnType(proxy, target, method, retVal);
                return retVal;
            } finally {
                if (target != null && !targetSource.isStatic()) {
                    targetSource.releaseTarget(target);
                }
                if (setProxyContext) {
                    AopContext.setCurrentProxy(oldProxy);
                }
            }
        }

        @Override
        public boolean equals(Object other) {
            return (this == other ||
                    (other instanceof DynamicAdvisedInterceptor &&
                            this.advised.equals(((DynamicAdvisedInterceptor) other).advised)));
        }

        @Override
        public int hashCode() {
            return this.advised.hashCode();
        }
    }

    private static class CglibMethodInvocation extends ReflectiveMethodInvocation {

        private final MethodProxy methodProxy;

        public CglibMethodInvocation(Object proxy, Object target, Method method, Object[] arguments, Class<?> targetClass,
                                     List<Object> interceptorsAndDynamicMethodMatchers, MethodProxy methodProxy) {
            super(proxy, target, method, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
            //method 是 public ，且不是声明在 Object类的方法，不是 equals、hashcode、toString方法
            this.methodProxy = (Modifier.isPublic(method.getModifiers()) &&
                    method.getDeclaringClass() != Object.class && !ClassUtils.isEqualsMethod(method) &&
                    !ClassUtils.isHashCodeMethod(method) && !ClassUtils.isToStringMethod(method) ?
                    methodProxy : null);
        }

        //如果是 public 方法直接调用，稍微提升下性能
        @Override
        protected Object invokeJoinpoint() throws Throwable {
            if (this.methodProxy != null) {
                return this.methodProxy.invoke(this.target, this.arguments);
            } else {
                return super.invokeJoinpoint();
            }
        }
    }


    private static class ClassLoaderAwareUndeclaredThrowableStrategy extends UndeclaredThrowableStrategy {

        private final ClassLoader classLoader;

        public ClassLoaderAwareUndeclaredThrowableStrategy(ClassLoader classLoader) {
            super(UndeclaredThrowableException.class);
            this.classLoader = classLoader;
        }

        @Override
        public byte[] generate(ClassGenerator cg) throws Exception {
            if (this.classLoader == null) {
                return super.generate(cg);
            }

            Thread currentThread = Thread.currentThread();
            ClassLoader threadContextClassLoader;
            try {
                threadContextClassLoader = currentThread.getContextClassLoader();
            } catch (Throwable ex) {
                return super.generate(cg);
            }

            boolean overrideClassLoader = !this.classLoader.equals(threadContextClassLoader);
            if (overrideClassLoader) {
                currentThread.setContextClassLoader(this.classLoader);
            }
            try {
                return super.generate(cg);
            } finally {
                if (overrideClassLoader) {
                    currentThread.setContextClassLoader(threadContextClassLoader);
                }
            }
        }
    }
}
