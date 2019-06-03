package com.qinwei.deathnote.support.resolve;

import com.qinwei.deathnote.utils.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-06-03
 */
public abstract class AbstractPropertyResolver implements PropertyResolver {

    private String placeholderPrefix;

    private String placeholderSuffix;

    @Override
    public Set<String> findPlaceholders(String text) {
        int prefixCount = StringUtils.countMatchString(text, placeholderPrefix);
        int suffixCount = StringUtils.countMatchString(text, placeholderSuffix);
        if (prefixCount != suffixCount) {
            throw new IllegalArgumentException("Unable to resolve  '" + text + "' ,because the placeholderPrefix '"
                    + placeholderPrefix + "' don't match the placeholderSuffix '" + placeholderSuffix + "'");
        }
        Set<String> result = new HashSet<>();
        recursiveFindPlaceholder(text, result);
        return result;
    }

    private void recursiveFindPlaceholder(String text, Set<String> result) {
        int startIndex = text.indexOf(placeholderPrefix);
        if (startIndex != -1) {
            int endIndex = text.indexOf(placeholderSuffix);
            if (endIndex != -1) {
                String placeholder = text.substring(startIndex + placeholderPrefix.length(), endIndex);
                result.add(placeholder);
                String substring = text.substring(endIndex + placeholderSuffix.length());
                if (StringUtils.isEmpty(substring)) {
                    return;
                }
                recursiveFindPlaceholder(substring, result);
            }
        }
    }

    @Override
    public String resolvePlaceholders(String text, Map<String, Object> config) {
        String resolvedString = text;
        Set<String> placeholders = findPlaceholders(resolvedString);
        for (String placeholder : placeholders) {
            String key = placeholderPrefix + placeholder + placeholderSuffix;
            String value = (String) config.get(placeholder);
            resolvedString = resolvedString.replace(key, value == null ? key : value);
        }
        return resolvedString;
    }

    public void setPlaceholderPrefix(String placeholderPrefix) {
        assert !StringUtils.isEmpty(placeholderPrefix) : "placeholderPrefix can not be empty";
        this.placeholderPrefix = placeholderPrefix;
    }

    public void setPlaceholderSuffix(String placeholderSuffix) {
        assert !StringUtils.isEmpty(placeholderSuffix) : "placeholderSuffix can not be empty";
        this.placeholderSuffix = placeholderSuffix;
    }

}
