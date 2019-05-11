package com.qinwei.deathnote.config.convert;

import lombok.extern.slf4j.Slf4j;

/**
 * @author qinwei
 * @date 2019-05-10
 */
@Slf4j
public class StringToIntegerConverter implements Converter<String, Integer> {

    @Override
    public Integer convert(String source) {
        try {
            return Integer.valueOf(source);
        } catch (NumberFormatException e) {
            log.error("can't convert {} to {}", source, Integer.class);
        }
        return null;
    }
}
