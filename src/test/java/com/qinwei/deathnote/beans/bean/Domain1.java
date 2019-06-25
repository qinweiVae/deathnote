package com.qinwei.deathnote.beans.bean;

import com.alibaba.fastjson.JSON;
import com.qinwei.deathnote.context.annotation.Component;
import com.qinwei.deathnote.context.annotation.Scope;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author qinwei
 * @date 2019-05-29
 */
@Getter
@Setter
@Component
@Scope("prototype")
public class Domain1 {

    private String brand;

    private String speed;

    private List<String> list;

    private String date;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
