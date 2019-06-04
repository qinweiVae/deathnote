package com.qinwei.deathnote.beans;

import com.google.common.collect.Lists;
import com.qinwei.deathnote.beans.registry.DefaultSingletonBeanRegistry;
import com.qinwei.deathnote.utils.BeanUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-05-29
 */
public class BeanTest {

    @Test
    public void testCopyProperties() {
        TestBean source = new TestBean();
        source.setAge(32);
        source.setBeanName("qw");
        source.setDate(new Date());
        source.setDestroyed(true);
        source.setFriends(Lists.newArrayList("1", "2", "3"));
        source.setMyFloat(4f);
        source.setSomeBoolean(true);
        Map map = new HashMap();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        source.setSomeMap(map);

        Domain1 domain = new Domain1();
        domain.setBrand("abc");
        domain.setSpeed(123);
        domain.setList(Lists.newArrayList("z", "x", "v"));

        source.setDomain(domain);

        TestBean target = new TestBean();
        BeanUtils.copyProperties(source, target);
        System.out.println(source);
        System.out.println(target);

        Domain2 domain2 = new Domain2();
        BeanUtils.copyProperties(domain, domain2);
        System.out.println(domain2);
    }

    @Test
    public void testRegisterDependentBean() {
        DefaultSingletonBeanRegistry registry = new DefaultSingletonBeanRegistry();
        // a depend on b,c,d
        // e depend on a
        // f depend a
        registry.registerDependentBean("b", "a");
        registry.registerDependentBean("c", "a");
        registry.registerDependentBean("d", "a");
        registry.registerDependentBean("a", "e");
        registry.registerDependentBean("a", "f");

        String beanName = "a";
        String[] dependentBeans = registry.getDependentBeans(beanName);
        System.out.println("获取依赖 " + beanName + " 的所有bean : " + Arrays.toString(dependentBeans));

        String[] dependenciesForBean = registry.getDependenciesForBean(beanName);
        System.out.println("获取 " + beanName + " 的所有依赖bean : " + Arrays.toString(dependenciesForBean));

        System.out.println(registry.isDependent("b","a"));
    }

}
