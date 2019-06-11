package com.qinwei.deathnote.beans.postprocessor;

import com.qinwei.deathnote.context.AbstractApplicationContext;
import com.qinwei.deathnote.context.event.ApplicationEventMulticaster;
import com.qinwei.deathnote.context.event.ApplicationListener;

/**
 * @author qinwei
 * @date 2019-06-10
 */
public class ApplicationListenerDetector implements DestructionAwareBeanPostProcessor {

    private final AbstractApplicationContext applicationContext;

    public ApplicationListenerDetector(AbstractApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) {
        if (bean instanceof ApplicationListener) {
            try {
                ApplicationEventMulticaster multicaster = this.applicationContext.getApplicationEventMulticaster();
                multicaster.removeApplicationListener((ApplicationListener<?>) bean);
                multicaster.removeApplicationListenerBean(beanName);
            } catch (Exception ignore) {

            }
        }
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return bean instanceof ApplicationListener;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof ApplicationListener) {
            //如果是单例则添加
            boolean singleton = this.applicationContext.isSingleton(beanName);
            if (singleton) {
                this.applicationContext.addApplicationListener((ApplicationListener<?>) bean);
            }
        }
        return bean;
    }
}
