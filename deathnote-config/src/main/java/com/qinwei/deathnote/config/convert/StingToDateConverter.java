package com.qinwei.deathnote.config.convert;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author qinwei
 * @date 2019-05-10
 */
@Slf4j
public class StingToDateConverter implements Converter<String, Date> {

    @Override
    public Date convert(String source) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            return format.parse(source);
        } catch (ParseException e) {
            log.error("can't convert {} to {}", source, Date.class);
        }
        return null;
    }
}
