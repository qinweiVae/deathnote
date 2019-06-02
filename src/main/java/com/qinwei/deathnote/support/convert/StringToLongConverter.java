package com.qinwei.deathnote.support.convert;

import lombok.extern.slf4j.Slf4j;

/**
 * @author qinwei
 * @date 2019-05-10
 */
@Slf4j
public class StringToLongConverter implements Converter<String, Long> {

    @Override
    public Long convert(String source) {
        try {
            return Long.valueOf(source);
        } catch (NumberFormatException e) {
            log.error("can't convert {} to {}", source, Long.class);
        }
        return null;
    }
}
