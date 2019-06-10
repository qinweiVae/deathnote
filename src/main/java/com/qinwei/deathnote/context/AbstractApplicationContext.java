package com.qinwei.deathnote.context;

import com.qinwei.deathnote.beans.DefaultListableBeanFactory;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.beans.postprocessor.BeanPostProcessor;
import com.qinwei.deathnote.config.Config;
import com.qinwei.deathnote.config.StandardConfig;
import com.qinwei.deathnote.context.event.ApplicationEvent;
import com.qinwei.deathnote.context.event.ApplicationListener;
import com.qinwei.deathnote.context.event.ContextStartedEvent;
import com.qinwei.deathnote.context.event.ContextStoppedEvent;
import com.qinwei.deathnote.support.scan.ResourcesScanner;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public class AbstractApplicationContext implements ApplicationContext {

    private final AtomicBoolean active = new AtomicBoolean();

    private final AtomicBoolean closed = new AtomicBoolean();

    private final Object monitor = new Object();

    private final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();

    private Thread shutdownHook;

    private Config config;

    private final ResourcesScanner resourcesScanner;

    private final ConfigurableListableBeanFactory beanFactory;

    private long startup;

    private Set<ApplicationEvent> earlyApplicationEvents;

    public AbstractApplicationContext() {
        this.resourcesScanner = ResourcesScanner.getInstance();
        this.config = getConfig();
        this.beanFactory = getBeanFactory();
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
    public void start() {
        publishEvent(new ContextStartedEvent(this));
    }

    @Override
    public void stop() {
        publishEvent(new ContextStoppedEvent(this));
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void publishEvent(Object event) {

    }

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        this.applicationListeners.add(listener);
    }

    @Override
    public void refresh() {
        synchronized (monitor) {
            prepareRefresh();

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
                e.printStackTrace();
            } finally {
            }
        }
    }

    protected void finishRefresh() {

    }

    protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {

    }

    protected void registerListeners() {

    }

    protected void onRefresh() {
        //留给子类实现,springboot 就是在这一步创建 tomcat、jetty、netty等web容器
    }

    protected void initApplicationEventMulticaster() {

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

        this.earlyApplicationEvents = new LinkedHashSet<>();
    }


    protected void doClose() {

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

    public Collection<ApplicationListener<?>> getApplicationListeners() {
        return this.applicationListeners;
    }

    @Override
    public ConfigurableListableBeanFactory getBeanFactory() {
        return new DefaultListableBeanFactory();
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
        return beanFactory.getBeanNamesForType(type);
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons) {
        assertBeanFactoryActive();
        return beanFactory.getBeanNamesForType(type, includeNonSingletons);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        assertBeanFactoryActive();
        return beanFactory.getBeansOfType(type);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons) {
        assertBeanFactoryActive();
        return beanFactory.getBeansOfType(type, includeNonSingletons);
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
        assertBeanFactoryActive();
        return beanFactory.getBeansWithAnnotation(annotationType);
    }

    @Override
    public <T extends Annotation> T findAnnotationOnBean(String beanName, Class<T> annotationType) {
        assertBeanFactoryActive();
        return beanFactory.findAnnotationOnBean(beanName, annotationType);
    }

    @Override
    public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
        assertBeanFactoryActive();
        return beanFactory.getBeanNamesForAnnotation(annotationType);
    }

    @Override
    public Object getBean(String name) {
        assertBeanFactoryActive();
        return beanFactory.getBean(name);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) {
        assertBeanFactoryActive();
        return beanFactory.getBean(name, requiredType);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        assertBeanFactoryActive();
        return beanFactory.getBean(requiredType);
    }

    @Override
    public String[] getAliases(String name) {
        return beanFactory.getAliases(name);
    }

    @Override
    public boolean containsBean(String name) {
        return beanFactory.containsBean(name);
    }

    @Override
    public boolean isTypeMatch(String name, Class<?> typeToMatch) {
        assertBeanFactoryActive();
        return beanFactory.isTypeMatch(name, typeToMatch);
    }

    @Override
    public Class<?> getType(String name) {
        assertBeanFactoryActive();
        return beanFactory.getType(name);
    }

    @Override
    public boolean isSingleton(String name) {
        assertBeanFactoryActive();
        return beanFactory.isSingleton(name);
    }

    @Override
    public boolean isPrototype(String name) {
        assertBeanFactoryActive();
        return beanFactory.isPrototype(name);
    }
}
