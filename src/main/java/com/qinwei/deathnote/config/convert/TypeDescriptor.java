package com.qinwei.deathnote.config.convert;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author qinwei
 * @date 2019-05-11 23:40
 */
@Getter
@Setter
@AllArgsConstructor
public class TypeDescriptor {

    private Class targetType;

    private Class sourceType;

    private Converter converter;

}
