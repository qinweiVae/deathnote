package com.qinwei.deathnote.bean;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-05-29
 */
@Getter
@Setter
public class TestBean {

    private Domain domain;

    private String beanName;

    private boolean postProcessed;

    private String sex;

    private int age;

    private String[] stringArray;

    private Integer[] someIntegerArray;

    private Integer[][] nestedIntegerArray;

    private int[] someIntArray;

    private int[][] nestedIntArray;

    private Date date;

    private Float myFloat;

    private Collection<? super Object> friends;

    private Set<?> someSet = new HashSet<>();

    private Map<?, ?> someMap = new HashMap<>();

    private List<?> someList = new ArrayList<>();

    private boolean destroyed;

    private Number someNumber;

    private Boolean someBoolean;

    public TestBean() {
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
