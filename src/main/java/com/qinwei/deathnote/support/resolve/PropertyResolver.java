package com.qinwei.deathnote.support.resolve;

import java.util.Map;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-03
 */
public interface PropertyResolver {

    String resolvePlaceholders(String text, Map<String, Object> config);

    Set<String> findPlaceholders(String text);
}
