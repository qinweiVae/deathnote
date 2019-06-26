package com.qinwei.deathnote.context;

import com.qinwei.deathnote.beans.bean.Domain1;
import com.qinwei.deathnote.beans.bean.Domain2;
import com.qinwei.deathnote.beans.factory.BeanFactory;
import com.qinwei.deathnote.config.Config;
import com.qinwei.deathnote.context.annotation.AutowiredService;
import com.qinwei.deathnote.context.annotation.PropertyDescriptorService;
import com.qinwei.deathnote.context.aware.AwareTest;
import com.qinwei.deathnote.context.event.ApplicationEventPublisher;
import com.qinwei.deathnote.support.spi.Worker;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author qinwei
 * @date 2019-06-21
 */
@Slf4j
public class ContextTest extends BaseTest {

    @Test
    public void testBeanLifeCycle() {
        log.warn(context.toString());
    }

    @Test
    public void testBeanDefinition() {
        String[] beanDefinitionNames = context.getBeanDefinitionNames();
        for (String beanName : beanDefinitionNames) {
            Object bean = context.getBean(beanName);
            log.info("{} : {} , is singleton : {}", beanName, bean.getClass().getName(), context.isSingleton(beanName));
        }
    }

    @Test
    public void testBeanNameForType() {
        for (String beanName : context.getBeanNamesForType(Worker.class)) {
            log.info("{} is singleton : {}", beanName, context.isSingleton(beanName));
        }
    }

    @Test
    public void testPublisherEvent() {
        Domain1 domain1 = new Domain1();
        domain1.setBrand("vae");
        context.publishEvent(domain1);

        Domain2 domain2 = new Domain2();
        domain2.setBrand("qinwei");
        context.publishEvent(domain2);
    }

    @Test
    public void testAware() {
        AwareTest aware = (AwareTest) this.context.getBean("aware");
        ApplicationContext applicationContext = aware.getApplicationContext();
        BeanFactory beanFactory = aware.getBeanFactory();
        Config config = aware.getConfig();
        ApplicationEventPublisher publisher = aware.getApplicationEventPublisher();
    }

    @Test
    public void testPropertyDescriptor() {
        log.info("----------------- PropertyDescriptorService ------------------");
        PropertyDescriptorService pd = context.getBean(PropertyDescriptorService.class);
        pd.work();
        pd.workMap();
        pd.workCollection();
        pd.workArray();
    }

    @Test
    public void testAutowired() {
        log.info("----------------- AutowiredService ------------------");
        AutowiredService autowired = context.getBean(AutowiredService.class);
        autowired.work();
        autowired.workMap();
        autowired.workCollection();
        autowired.workArray();
    }
}
