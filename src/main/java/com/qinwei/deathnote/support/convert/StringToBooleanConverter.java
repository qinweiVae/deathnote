package com.qinwei.deathnote.support.convert;

import com.qinwei.deathnote.utils.StringUtils;

/**
 * @author qinwei
 * @date 2019-05-10
 */
public class StringToBooleanConverter implements Converter<String, Boolean> {

    @Override
    public Boolean convert(String source) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }
        if ("true".equalsIgnoreCase(source)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
