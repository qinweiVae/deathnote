package com.qinwei.deathnote.beans.bean;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

/**
 * @author qinwei
 * @date 2019-06-04
 */
@Getter
@Setter
public class Domain2 {

    private String Brand;

    private Integer Speed;

    private Collection<String> List;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
