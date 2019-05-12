package com.qinwei.deathnote.config.convert;

import com.qinwei.deathnote.support.scan.AnnotationScanner;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
        try {
            AnnotationScanner scanner = new AnnotationScanner();
            List<Class> classes = scanner.scan(Convert.class);
            for (Class clazz : classes) {
                addConvert((Converter) clazz.newInstance());
            }
        } catch (Exception e) {
            log.error("can not instantiation class ...", e);
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
