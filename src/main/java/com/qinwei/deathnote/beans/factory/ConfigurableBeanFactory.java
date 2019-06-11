package com.qinwei.deathnote.beans.factory;

import com.qinwei.deathnote.beans.postprocessor.BeanPostProcessor;
import com.qinwei.deathnote.support.convert.Conversion;

/**
 * @author qinwei
 * @date 2019-05-21
 */
public interface ConfigurableBeanFactory extends BeanFactory {

    String SCOPE_SINGLETON = "singleton";

    String SCOPE_PROTOTYPE = "prototype";

    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

    void destroyBean(String beanName, Object beanInstance);

    void destroySingletons();

    void registerDependentBean(String beanName, String dependentBeanName);

    String[] getDependentBeans(String beanName);

    String[] getDependenciesForBean(String beanName);

    Conversion getConversion();
}
