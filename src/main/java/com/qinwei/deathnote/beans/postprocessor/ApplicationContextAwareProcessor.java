package com.qinwei.deathnote.beans.postprocessor;

import com.qinwei.deathnote.context.ApplicationContext;
import com.qinwei.deathnote.context.aware.ApplicationContextAware;
import com.qinwei.deathnote.context.aware.ApplicationEventPublisherAware;
import com.qinwei.deathnote.context.aware.Aware;
import com.qinwei.deathnote.context.aware.ConfigAware;

/**
 * @author qinwei
 * @date 2019-06-10
 */
public class ApplicationContextAwareProcessor implements BeanPostProcessor {

    private final ApplicationContext applicationContext;

    public ApplicationContextAwareProcessor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof Aware) {
            if (bean instanceof ApplicationContextAware) {
                ((ApplicationContextAware) bean).setApplicationContext(this.applicationContext);
            }
            if (bean instanceof ApplicationEventPublisherAware) {
                ((ApplicationEventPublisherAware) bean).setApplicationEventPublisher(this.applicationContext);
            }
            if (bean instanceof ConfigAware) {
                ((ConfigAware) bean).setConfig(this.applicationContext.getConfig());
            }
        }
        return bean;
    }

}
