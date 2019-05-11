package com.qinwei.deathnote.config.convert;

/**
 * @author qinwei
 * @date 2019-05-10
 */
public class DefaultConversion extends AbstractConversion {

    public static DefaultConversion getInstance() {
        return LazyHolder.INSTANCE;
    }

    private DefaultConversion() {
        addDefaultConverter();
        initCache();
    }

    private void addDefaultConverter() {
        addConvert(new StringToIntegerConverter());
        addConvert(new StringToLongConverter());
        addConvert(new StingToDateConverter());
        addConvert(new StringToBooleanConverter());
    }

    private static class LazyHolder {
        private static final DefaultConversion INSTANCE = new DefaultConversion();
    }

}
