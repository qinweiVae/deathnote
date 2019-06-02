package com.qinwei.deathnote.support.convert;

import com.qinwei.deathnote.support.scan.TypeAnnotationScanner;
import com.qinwei.deathnote.utils.ClassUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * @author qinwei
 * @date 2019-05-10
 */
@Slf4j
public class DefaultConversion extends AbstractConversion {

    public static DefaultConversion getInstance() {
        return LazyHolder.INSTANCE;
    }

    private DefaultConversion() {
        addDefaultConverter();
        addCustomConverter();
        initCache();
    }

    private void addCustomConverter() {
        TypeAnnotationScanner scanner = new TypeAnnotationScanner();
        Set<Class> classes = scanner.scan(Convert.class);
        for (Class clazz : classes) {
            addConvert((Converter) ClassUtils.instantiateClass(clazz));
        }
    }

    private void addDefaultConverter() {
        addConvert(new StringToIntegerConverter());
        addConvert(new StringToLongConverter());
        addConvert(new StringToBooleanConverter());
    }

    private static class LazyHolder {
        private static final DefaultConversion INSTANCE = new DefaultConversion();
    }

}
