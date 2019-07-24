package com.qinwei.deathnote.context.aop;

import com.qinwei.deathnote.beans.bean.Domain;
import com.qinwei.deathnote.beans.bean.FactoryBean;

/**
 * @author qinwei
 * @date 2019-07-23
 */
//@Component
public class FactoryBeanTest implements FactoryBean<Domain> {

    private Domain domain;

    @Override
    public Domain getObject() throws Exception {
        if (domain == null) {
            domain = new Domain();
            domain.setBeanName("qinwei");
        }
        return domain;
    }

    @Override
    public Class<?> getObjectType() {
        return Domain.class;
    }
}
