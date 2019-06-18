package com.qinwei.deathnote.beans.bean;

import lombok.Getter;
import lombok.Setter;
import com.qinwei.deathnote.utils.StringUtils;

/**
 * @author qinwei
 * @date 2019-05-24
 */
@Getter
@Setter
public class BeanDefinitionHolder {

    private final BeanDefinition beanDefinition;

    private final String beanName;

    private final String[] aliases;

    public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName) {
        this(beanDefinition, beanName, null);
    }

    public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName, String[] aliases) {
        assert beanDefinition != null : "BeanDefinition must not be null";
        assert !StringUtils.isEmpty(beanName) : "beanName must not be null";

        this.beanDefinition = beanDefinition;
        this.beanName = beanName;
        this.aliases = aliases;
    }
}
