package com.qinwei.deathnote.context.postprocessor;

import com.qinwei.deathnote.beans.postprocessor.InstantiationAwareBeanPostProcessor;
import com.qinwei.deathnote.context.annotation.Component;
import com.qinwei.deathnote.context.annotation.Order;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author qinwei
 * @date 2019-06-25
 */
@Component
@Order(value = 1)
@Slf4j
public class BeanPostProcessorTest implements InstantiationAwareBeanPostProcessor {

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
        if ("lifeCycle".equals(beanName)) {
            log.info("bean name : {} postProcessBeforeInstantiation()", beanName);
        }
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) {
        if ("lifeCycle".equals(beanName)) {
            log.info("bean name : {} postProcessAfterInstantiation()", beanName);
        }
        return true;
    }

    @Override
    public Map<String, Object> postProcessProperties(Map<String, Object> properties, Object bean, String beanName) {
        if ("lifeCycle".equals(beanName)) {
            log.info("bean name : {}  postProcessProperties()", beanName);
        }
        return properties;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if ("lifeCycle".equals(beanName)) {
            log.info("bean name : {}  before initialization", beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if ("lifeCycle".equals(beanName)) {
            log.info("bean name : {}  after initialization", beanName);
        }
        return bean;
    }
}
