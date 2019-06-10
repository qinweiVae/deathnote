package com.qinwei.deathnote.context;

import com.qinwei.deathnote.beans.DefaultListableBeanFactory;
import com.qinwei.deathnote.beans.factory.ConfigurableListableBeanFactory;
import com.qinwei.deathnote.config.Config;
import com.qinwei.deathnote.config.StandardConfig;
import com.qinwei.deathnote.context.event.ApplicationListener;
import com.qinwei.deathnote.support.scan.ResourcesScanner;

import java.lang.annotation.Annotation;
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

    public AbstractApplicationContext() {
        this.resourcesScanner = ResourcesScanner.getInstance();
        this.config = getConfig();
        this.beanFactory = getBeanFactory();
    }

    @Override
    public Config getConfig() {
        if (this.config == null) {
            this.config = createConfig();
        }
        return this.config;
    }

    protected Config createConfig() {
        return StandardConfig.getInstance();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

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
