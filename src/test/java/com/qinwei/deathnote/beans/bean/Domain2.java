package com.qinwei.deathnote.beans.bean;

import com.alibaba.fastjson.JSON;
import com.qinwei.deathnote.context.annotation.Component;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Date;

/**
 * @author qinwei
 * @date 2019-06-04
 */
@Getter
@Setter
@Component
public class Domain2 {

    private String Brand;

    private Integer Speed;

    private Collection<String> List;

    private Date date;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
