package com.qinwei.deathnote.context;

import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.beans.postprocessor.ApplicationContextAwareProcessor;
import com.qinwei.deathnote.beans.postprocessor.ApplicationListenerDetector;
import com.qinwei.deathnote.beans.postprocessor.BeanDefinitionRegistryPostProcessor;
import com.qinwei.deathnote.beans.postprocessor.BeanFactoryPostProcessor;
import com.qinwei.deathnote.beans.postprocessor.BeanPostProcessor;
import com.qinwei.deathnote.beans.registry.BeanDefinitionRegistry;
import com.qinwei.deathnote.config.Config;
import com.qinwei.deathnote.config.StandardConfig;
import com.qinwei.deathnote.context.annotation.AnnotationOrderComparator;
import com.qinwei.deathnote.context.event.ApplicationEvent;
import com.qinwei.deathnote.context.event.ApplicationEventMulticaster;
import com.qinwei.deathnote.context.event.ApplicationListener;
import com.qinwei.deathnote.context.event.ContextClosedEvent;
import com.qinwei.deathnote.context.event.ContextRefreshedEvent;
import com.qinwei.deathnote.context.event.ContextStartedEvent;
import com.qinwei.deathnote.context.event.ContextStoppedEvent;
import com.qinwei.deathnote.context.event.PayloadApplicationEvent;
import com.qinwei.deathnote.context.event.SimpleApplicationEventMulticaster;
import com.qinwei.deathnote.context.lifecycle.DefaultLifecycleProcessor;
import com.qinwei.deathnote.context.lifecycle.LifecycleProcessor;
import com.qinwei.deathnote.support.scan.ResourcesScanner;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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

    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new ArrayList<>();

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

    @Override
    public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor) {
        assert postProcessor != null : "BeanFactoryPostProcessor must not be null";
        this.beanFactoryPostProcessors.add(postProcessor);
    }

    public List<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
        return this.beanFactoryPostProcessors;
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

    /**
     * 发送事件消息
     */
    @Override
    public void publishEvent(Object event) {
        assert event != null : "Event must not be null";

        ApplicationEvent applicationEvent;
        if (event instanceof ApplicationEvent) {
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

                invokeBeanFactoryPostProcessors(beanFactory);

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

    /**
     * 执行 BeanFactoryPostProcessor
     */
    protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {

        Set<String> processedBeans = new HashSet<>();

        //DefaultListableBeanFactory 是 BeanDefinitionRegistry 的实现类
        if (beanFactory instanceof BeanDefinitionRegistry) {
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
            List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
            List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

            // 执行 BeanDefinitionRegistryPostProcessor 的 postProcessBeanDefinitionRegistry 方法
            for (BeanFactoryPostProcessor postProcessor : getBeanFactoryPostProcessors()) {
                if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
                    BeanDefinitionRegistryPostProcessor registryPostProcessor = (BeanDefinitionRegistryPostProcessor) postProcessor;
                    registryPostProcessor.postProcessBeanDefinitionRegistry(registry);
                    registryProcessors.add(registryPostProcessor);
                } else {
                    regularPostProcessors.add(postProcessor);
                }
            }

            // 从所有的 bean 中找到 BeanDefinitionRegistryPostProcessor 的 bean
            List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();
            String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class);
            for (String name : postProcessorNames) {
                if (!processedBeans.contains(name)) {
                    currentRegistryProcessors.add(beanFactory.getBean(name, BeanDefinitionRegistryPostProcessor.class));
                    processedBeans.add(name);
                }
            }
            //将BeanDefinitionRegistryPostProcessor 按照 @Order 注解排序
            AnnotationOrderComparator.sort(currentRegistryProcessors);
            registryProcessors.addAll(currentRegistryProcessors);
            // 执行 BeanDefinitionRegistryPostProcessor 的 postProcessBeanDefinitionRegistry 方法
            for (BeanDefinitionRegistryPostProcessor processor : currentRegistryProcessors) {
                processor.postProcessBeanDefinitionRegistry(registry);
            }
            //执行BeanFactoryPostProcessor 的 postProcessBeanFactory 方法
            postProcessBeanFactory(registryProcessors, beanFactory);
            postProcessBeanFactory(regularPostProcessors, beanFactory);
        } else {
            //执行BeanFactoryPostProcessor 的 postProcessBeanFactory 方法
            postProcessBeanFactory(getBeanFactoryPostProcessors(), beanFactory);
        }

        // 从所有的 bean 中找到 BeanFactoryPostProcessor 的 bean
        List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>();
        String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class);
        for (String name : postProcessorNames) {
            if (!processedBeans.contains(name)) {
                orderedPostProcessors.add(beanFactory.getBean(name, BeanFactoryPostProcessor.class));
            }
        }
        //将BeanFactoryPostProcessor 按照 @Order 注解排序
        AnnotationOrderComparator.sort(orderedPostProcessors);
        //执行BeanFactoryPostProcessor 的 postProcessBeanFactory 方法
        postProcessBeanFactory(orderedPostProcessors, beanFactory);
    }

    /**
     * 执行BeanFactoryPostProcessor 的 postProcessBeanFactory 方法
     */
    private void postProcessBeanFactory(List<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {
        for (BeanFactoryPostProcessor postProcessor : postProcessors) {
            postProcessor.postProcessBeanFactory(beanFactory);
        }
    }

    protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
        refreshBeanFactory();
        return getBeanFactory();
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
        List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
        String[] beanNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class);
        for (String beanName : beanNames) {
            BeanPostProcessor postProcessor = beanFactory.getBean(beanName, BeanPostProcessor.class);
            orderedPostProcessors.add(postProcessor);
        }
        //按照 @Order 注解排序
        AnnotationOrderComparator.sort(orderedPostProcessors);
        //添加 BeanPostProcessor
        registerBeanPostProcessors(beanFactory, orderedPostProcessors);
        //重新注册用来自动探测内部ApplicationListener的post-processor，这样可以将他们移到处理器链条的末尾
        beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));
    }

    private void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> orderedPostProcessors) {
        for (BeanPostProcessor postProcessor : orderedPostProcessors) {
            beanFactory.addBeanPostProcessor(postProcessor);
        }
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

    @Override
    public String[] getBeanDefinitionNames() {
        return getBeanFactory().getBeanDefinitionNames();
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return getBeanFactory().containsBeanDefinition(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return getBeanFactory().getBeanDefinitionCount();
    }

    //---------------------------------------------------------------------------------------------------------------------------

    @Override
    public abstract ConfigurableListableBeanFactory getBeanFactory();

    protected abstract void refreshBeanFactory();


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ApplicationContext");
        sb.append(": startup date [")
                .append(new Date(this.startup))
                .append("]; ");
        return sb.toString();
    }
}
