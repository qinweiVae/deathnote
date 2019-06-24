package com.qinwei.deathnote.beans.bean;

import java.lang.reflect.Member;
import java.util.HashSet;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-05-24
 */
public class RootBeanDefinition extends AbstractBeanDefinition {

    private final Object lock = new Object();

    private BeanDefinitionHolder decoratedDefinition;

    private Set<Member> externallyConfigMembers;

    private Set<String> externallyInitMethods;

    private Set<String> externallyDestroyMethods;

    public RootBeanDefinition() {
    }

    public RootBeanDefinition(Class<?> beanClass) {
        setBeanClass(beanClass);
    }

    public RootBeanDefinition(String beanClassName) {
        setBeanClassName(beanClassName);
    }

    public RootBeanDefinition(BeanDefinition beanDefinition) {
        setBeanClassName(beanDefinition.getBeanClassName());
        setScope(beanDefinition.getScope());
        setAbstract(beanDefinition.isAbstract());
        setDependsOn(beanDefinition.getDependsOn());
        setPrimary(beanDefinition.isPrimary());
        setInitMethodName(beanDefinition.getInitMethodName());
        setDestroyMethodName(beanDefinition.getDestroyMethodName());
        if (beanDefinition instanceof AbstractBeanDefinition) {
            if (((AbstractBeanDefinition) beanDefinition).hasBeanClass()) {
                setBeanClass(beanDefinition.getBeanClass());
            }
            Boolean lazyInit = ((AbstractBeanDefinition) beanDefinition).getLazyInit();
            if (lazyInit != null) {
                setLazyInit(lazyInit);
            }
        }
    }

    public BeanDefinitionHolder getDecoratedDefinition() {
        return decoratedDefinition;
    }

    public void setDecoratedDefinition(BeanDefinitionHolder decoratedDefinition) {
        this.decoratedDefinition = decoratedDefinition;
    }

    public void registerExternallyConfigMember(Member configMember) {
        synchronized (lock) {
            if (externallyConfigMembers == null) {
                externallyConfigMembers = new HashSet<>(1);
            }
            externallyConfigMembers.add(configMember);
        }
    }

    public boolean isExternallyConfigMember(Member configMember) {
        synchronized (lock) {
            return externallyConfigMembers != null && externallyConfigMembers.contains(configMember);
        }
    }

    public void registerExternallyInitMethod(String initMethod) {
        synchronized (lock) {
            if (externallyInitMethods == null) {
                externallyInitMethods = new HashSet<>(1);
            }
            externallyInitMethods.add(initMethod);
        }
    }


    public boolean isExternallyInitMethod(String initMethod) {
        synchronized (lock) {
            return externallyInitMethods != null && externallyInitMethods.contains(initMethod);
        }
    }

    public void registerExternallyDestroyMethod(String destroyMethod) {
        synchronized (lock) {
            if (externallyDestroyMethods == null) {
                externallyDestroyMethods = new HashSet<>(1);
            }
            externallyDestroyMethods.add(destroyMethod);
        }
    }

    public boolean isExternallyDestroyMethod(String destroyMethod) {
        synchronized (lock) {
            return externallyDestroyMethods != null && externallyDestroyMethods.contains(destroyMethod);
        }
    }

    @Override
    protected RootBeanDefinition cloneBeanDefinition() {
        return new RootBeanDefinition(this);
    }
}
