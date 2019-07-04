package com.qinwei.deathnote.beans.bean;

import com.alibaba.fastjson.JSON;
import com.qinwei.deathnote.support.annotation.AnnotationA;
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
@AnnotationA
public class Domain extends BaseDomain {

    @AnnotationA
    private Domain1 domain;

    @AnnotationA
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

    private Collection<String> friends;

    private Set<Domain> someSet = new HashSet<>();

    private Map<String, List<Domain>> someMap = new HashMap<>();

    private List<Map<String, List<Domain>>> someList = new ArrayList<>();

    private boolean destroyed;

    private Number someNumber;

    private Boolean someBoolean;

    public Domain() {
    }

    public Domain(Domain domain, Number someNumber) {
    }

    public void writeMethod(Set<Domain> someSet, Domain domain, Collection<String> friends) {
        this.someSet = someSet;
        this.friends = friends;
    }

    @Override
    @AnnotationA
    public String toString() {
        return JSON.toJSONString(this);
    }

}
