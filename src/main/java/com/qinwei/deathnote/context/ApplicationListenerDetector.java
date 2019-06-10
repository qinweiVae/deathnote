package com.qinwei.deathnote.context;

import com.qinwei.deathnote.beans.postprocessor.DestructionAwareBeanPostProcessor;
import com.qinwei.deathnote.context.event.ApplicationListener;

/**
 * @author qinwei
 * @date 2019-06-10
 */
public class ApplicationListenerDetector implements DestructionAwareBeanPostProcessor {

    private final ApplicationContext applicationContext;

    public ApplicationListenerDetector(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) {
        if (bean instanceof ApplicationListener) {

        }
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return bean instanceof ApplicationListener;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof ApplicationListener) {
            boolean singleton = this.applicationContext.isSingleton(beanName);
            if (singleton) {
                this.applicationContext.addApplicationListener((ApplicationListener<?>) bean);
            }
        }
        return bean;
    }
}
