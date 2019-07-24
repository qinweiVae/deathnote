package com.qinwei.deathnote.aop.targetSource;

/**
 * @author qinwei
 * @date 2019-07-23
 */
public class SimpleBeanTargetSource extends AbstractBeanFactoryBasedTargetSource {

    @Override
    public Object getTarget() throws Exception {
        return getBeanFactory().getBean(getTargetBeanName());
    }
}
