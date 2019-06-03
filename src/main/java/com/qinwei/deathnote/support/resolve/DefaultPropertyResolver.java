package com.qinwei.deathnote.support.resolve;

/**
 * @author qinwei
 * @date 2019-06-03
 */
public class DefaultPropertyResolver extends AbstractPropertyResolver {

    public static final String PLACEHOLDER_PREFIX = "${";

    public static final String PLACEHOLDER_SUFFIX = "}";


    public DefaultPropertyResolver() {
        setPlaceholderPrefix(PLACEHOLDER_PREFIX);
        setPlaceholderSuffix(PLACEHOLDER_SUFFIX);
    }
}
