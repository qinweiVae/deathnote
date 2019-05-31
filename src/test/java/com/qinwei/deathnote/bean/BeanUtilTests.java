package com.qinwei.deathnote.bean;

import com.google.common.collect.Lists;
import com.qinwei.deathnote.utils.BeanUtils;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-05-29
 */
public class BeanUtilTests {

    @Test
    public void testCopyProperties() throws Exception {
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
        Domain domain = new Domain();
        domain.setBrand("abc");
        domain.setSpeed(123);
        domain.setList(Lists.newArrayList("z", "x", "v"));
        source.setDomain(domain);
        TestBean target = new TestBean();
        BeanUtils.copyProperties(source, target);
        System.out.println(source);
        System.out.println(target);
    }
}
