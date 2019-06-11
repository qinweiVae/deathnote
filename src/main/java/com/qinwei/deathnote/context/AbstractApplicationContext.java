package com.qinwei.deathnote.context;

import com.qinwei.deathnote.beans.DefaultListableBeanFactory;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.beans.postprocessor.ApplicationContextAwareProcessor;
import com.qinwei.deathnote.beans.postprocessor.ApplicationListenerDetector;
import com.qinwei.deathnote.beans.postprocessor.BeanPostProcessor;
import com.qinwei.deathnote.config.Config;
import com.qinwei.deathnote.config.StandardConfig;
import com.qinwei.deathnote.context.event.*;
import com.qinwei.deathnote.context.lifecycle.DefaultLifecycleProcessor;
import com.qinwei.deathnote.context.lifecycle.LifecycleProcessor;
import com.qinwei.deathnote.support.scan.ResourcesScanner;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author qinwei
 * @date 2019-05-22
 */
@Slf4j
public abstract class AbstractApplicationContext implements ApplicationContext {

    private static final String APPLICATION_EVENT_MULTICASTER = "applicationEventMulticaster";

    private static final String LIFECYCLE_PROCESSOR = "lifecycleProcessor";

    private final AtomicBoolean active = new AtomicBoolean();

    private final AtomicBoolean closed = new AtomicBoolean();

    private final Object monitor = new Object();

    private final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();

    private Thread shutdownHook;

    private Config config;

    private ResourcesScanner resourcesScanner;

    private ConfigurableListableBeanFactory beanFactory;

    private long startup;

    private ApplicationEventMulticaster applicationEventMulticaster;

    private LifecycleProcessor lifecycleProcessor;

    public AbstractApplicationContext() {

    }

    protected ResourcesScanner getResourcesScanner() {
        if (this.resourcesScanner == null) {
            this.resourcesScanner = ResourcesScanner.getInstance();
        }
        return this.resourcesScanner;
    }

    @Override
    public Config getConfig() {
        if (this.config == null) {
            this.config = createConfig();
            //初始化config
            this.config.initConfig();
        }
        return this.config;
    }

    protected Config createConfig() {
        return StandardConfig.getInstance();
    }

    public ApplicationEventMulticaster getApplicationEventMulticaster() throws IllegalStateException {
        assert this.applicationEventMulticaster != null : "ApplicationEventMulticaster not initialized - call 'refresh' before multicasting events via the context: " + this;
        return this.applicationEventMulticaster;
    }

    public LifecycleProcessor getLifecycleProcessor() throws IllegalStateException {
        assert this.lifecycleProcessor != null : "LifecycleProcessor not initialized - call 'refresh' before invoking lifecycle methods via the context: " + this;
        return this.lifecycleProcessor;
    }

    //--------------------------------------------------------------------------------------------------------

    @Override
    public void start() {
        getLifecycleProcessor().start();
        publishEvent(new ContextStartedEvent(this));
    }

    @Override
    public void stop() {
        getLifecycleProcessor().stop();
        publishEvent(new ContextStoppedEvent(this));
    }

    @Override
    public boolean isRunning() {
        return this.lifecycleProcessor != null && this.lifecycleProcessor.isRunning();
    }

    //--------------------------------------------------------------------------------------------------------

    @Override
    public void publishEvent(Object event) {
        assert event != null : "Event must not be null";

        ApplicationEvent applicationEvent;
        if (event instanceof ApplicationContextEvent) {
            applicationEvent = (ApplicationEvent) event;
        } else {
            applicationEvent = new PayloadApplicationEvent<>(this, event);
        }
        getApplicationEventMulticaster().multicastEvent(applicationEvent);
    }

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        assert listener != null : "ApplicationListener must not be null";
        if (this.applicationEventMulticaster != null) {
            this.applicationEventMulticaster.addApplicationListener(listener);
        }
        this.applicationListeners.add(listener);
    }

    @Override
    public void refresh() {

        synchronized (monitor) {
            prepareRefresh();

            ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

            prepareBeanFactory(beanFactory);

            try {
                postProcessBeanFactory(beanFactory);

                registerBeanPostProcessors(beanFactory);

                initApplicationEventMulticaster();

                onRefresh();

                registerListeners();

                finishBeanFactoryInitialization(beanFactory);

                finishRefresh();

            } catch (Exception e) {

                destroyBeans();

                cancelRefresh(e);

                throw e;

            } finally {

                resetCommonCaches();
            }
        }
    }

    protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
        refreshBeanFactory();
        return getBeanFactory();
    }

    protected void refreshBeanFactory() {
        if (this.beanFactory != null) {
            destroyBeans();
            this.beanFactory = null;
        }
        try {
            DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
            //加载BeanDefinition
            loadBeanDefinitions(beanFactory);

            this.beanFactory = beanFactory;
        } catch (Exception e) {
            throw new RuntimeException("Unable to parsing bean definition source");
        }
    }

    protected void resetCommonCaches() {

    }

    protected void cancelRefresh(Exception e) {
        this.active.set(false);
    }

    protected void destroyBeans() {
        getBeanFactory().destroySingletons();
    }

    protected void finishRefresh() {
        initLifecycleProcessor();

        getLifecycleProcessor().onRefresh();

        publishEvent(new ContextRefreshedEvent(this));
    }

    protected void initLifecycleProcessor() {
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();
        if (beanFactory.containsBean(LIFECYCLE_PROCESSOR)) {
            this.lifecycleProcessor = beanFactory.getBean(LIFECYCLE_PROCESSOR, LifecycleProcessor.class);
        } else {
            this.lifecycleProcessor = new DefaultLifecycleProcessor();
            beanFactory.registerSingleton(LIFECYCLE_PROCESSOR, this.lifecycleProcessor);
        }
    }

    protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
        //初始化所有非lazy的单例bean
        beanFactory.preInstantiateSingletons();
    }

    /**
     * 注册ApplicationListener，非单例也可以注册
     */
    protected void registerListeners() {
        //通过ApplicationListenerDetector 将所有的单例bean 添加到 applicationListeners
        for (ApplicationListener<?> applicationListener : getApplicationListeners()) {
            getApplicationEventMulticaster().addApplicationListener(applicationListener);
        }
        //找到所有ApplicationListener的bean name，包括非单例bean，添加到 applicationListenerBeans
        String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class);
        for (String beanName : listenerBeanNames) {
            getApplicationEventMulticaster().addApplicationListenerBean(beanName);
        }
    }

    protected void onRefresh() {
        //留给子类实现,springboot 就是在这一步创建 tomcat、jetty、netty等web容器
    }

    /**
     * 初始化ApplicationEventMulticaster
     */
    protected void initApplicationEventMulticaster() {
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();
        if (beanFactory.containsBean(APPLICATION_EVENT_MULTICASTER)) {
            this.applicationEventMulticaster = beanFactory.getBean(APPLICATION_EVENT_MULTICASTER, ApplicationEventMulticaster.class);
        } else {
            this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
            beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER, this.applicationEventMulticaster);
        }
    }

    /**
     * 初始化并注册所有的 BeanPostProcessor
     */
    protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        String[] beanNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class);
        for (String beanName : beanNames) {
            BeanPostProcessor postProcessor = beanFactory.getBean(beanName, BeanPostProcessor.class);
            beanFactory.addBeanPostProcessor(postProcessor);
        }
        beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));
    }

    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        //留给子类实现
    }

    protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // bean 初始化前执行 Aware 接口的方法
        beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
        //自动检测ApplicationListener , 如果是单例则添加
        beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));
        //默认注册config
        if (beanFactory.containsBean(CONFIG)) {
            beanFactory.registerSingleton(CONFIG, getConfig());
        }
    }

    /**
     * 开始初始化
     */
    protected void prepareRefresh() {
        this.startup = System.currentTimeMillis();
        this.closed.set(false);
        this.active.set(true);
    }


    protected void doClose() {
        if (active.get() && closed.compareAndSet(false, true)) {
            try {
                publishEvent(new ContextClosedEvent(this));
            } catch (Exception e) {
                log.warn("Exception thrown from ApplicationListener handling ContextClosedEvent", e);
            }

            if (this.lifecycleProcessor != null) {
                try {
                    this.lifecycleProcessor.onClose();
                } catch (Throwable ex) {
                    log.warn("Exception thrown from LifecycleProcessor on context close", ex);
                }
            }

            destroyBeans();

            onClose();

            active.set(false);
        }
    }

    protected void onClose() {
        //留给子类实现
    }

    @Override
    public void registerShutdownHook() {
        if (shutdownHook == null) {
            shutdownHook = new Thread(() -> {
                synchronized (monitor) {
                    doClose();
                }
            });
        }
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    @Override
    public void close() {
        synchronized (monitor) {
            doClose();
            if (shutdownHook != null) {
                try {
                    Runtime.getRuntime().removeShutdownHook(shutdownHook);
                } catch (Exception ignore) {

                }
            }
        }
    }

    public Collection<ApplicationListener<?>> getApplicationListeners() {
        return this.applicationListeners;
    }

    @Override
    public ConfigurableListableBeanFactory getBeanFactory() {
        if (this.beanFactory == null) {
            throw new IllegalStateException("BeanFactory not initialized or already closed - " +
                    "call 'refresh' before accessing beans via the ApplicationContext");
        }
        return this.beanFactory;
    }

    protected void assertBeanFactoryActive() {
        if (!this.active.get()) {
            if (this.closed.get()) {
                throw new IllegalStateException(this.getClass().getName() + " has been closed already");
            } else {
                throw new IllegalStateException(this.getClass().getName() + " has not been refreshed yet");
            }
        }
    }

    //---------------------------------------------------------------------------------------------------------------------------

    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        assertBeanFactoryActive();
        return getBeanFactory().getBeanNamesForType(type);
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons) {
        assertBeanFactoryActive();
        return getBeanFactory().getBeanNamesForType(type, includeNonSingletons);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        assertBeanFactoryActive();
        return getBeanFactory().getBeansOfType(type);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons) {
        assertBeanFactoryActive();
        return getBeanFactory().getBeansOfType(type, includeNonSingletons);
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
        assertBeanFactoryActive();
        return getBeanFactory().getBeansWithAnnotation(annotationType);
    }

    @Override
    public <T extends Annotation> T findAnnotationOnBean(String beanName, Class<T> annotationType) {
        assertBeanFactoryActive();
        return getBeanFactory().findAnnotationOnBean(beanName, annotationType);
    }

    @Override
    public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
        assertBeanFactoryActive();
        return getBeanFactory().getBeanNamesForAnnotation(annotationType);
    }

    @Override
    public Object getBean(String name) {
        assertBeanFactoryActive();
        return getBeanFactory().getBean(name);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) {
        assertBeanFactoryActive();
        return getBeanFactory().getBean(name, requiredType);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        assertBeanFactoryActive();
        return getBeanFactory().getBean(requiredType);
    }

    @Override
    public String[] getAliases(String name) {
        return getBeanFactory().getAliases(name);
    }

    @Override
    public boolean containsBean(String name) {
        return getBeanFactory().containsBean(name);
    }

    @Override
    public boolean isTypeMatch(String name, Class<?> typeToMatch) {
        assertBeanFactoryActive();
        return getBeanFactory().isTypeMatch(name, typeToMatch);
    }

    @Override
    public Class<?> getType(String name) {
        assertBeanFactoryActive();
        return getBeanFactory().getType(name);
    }

    @Override
    public boolean isSingleton(String name) {
        assertBeanFactoryActive();
        return getBeanFactory().isSingleton(name);
    }

    @Override
    public boolean isPrototype(String name) {
        assertBeanFactoryActive();
        return getBeanFactory().isPrototype(name);
    }

    //---------------------------------------------------------------------------------------------------------------------------

    protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory);


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ApplicationContext");
        sb.append(": startup date [")
                .append(new Date(this.startup))
                .append("]; ");
        return sb.toString();
    }
}
