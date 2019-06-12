package com.qinwei.deathnote.support.convert;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author qinwei
 * @date 2019-06-12
 */
@Slf4j
@Convert
public class StringToListConverter implements Converter<String, List<String>> {

    @Override
    public List<String> convert(String source) {
        List<String> list = new ArrayList<>();
        String[] array = source.split(",");
        Collections.addAll(list, array);
        return list;
    }

}
