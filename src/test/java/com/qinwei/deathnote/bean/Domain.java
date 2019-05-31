package com.qinwei.deathnote.bean;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author qinwei
 * @date 2019-05-29
 */
@Getter
@Setter
public class Domain {

    private String brand;

    private int speed;

    private List<String> list;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
