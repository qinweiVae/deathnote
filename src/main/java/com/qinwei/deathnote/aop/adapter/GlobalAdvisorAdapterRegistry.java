package com.qinwei.deathnote.aop.adapter;

/**
 * @author qinwei
 * @date 2019-07-12
 */
public class GlobalAdvisorAdapterRegistry {

    private GlobalAdvisorAdapterRegistry() {
    }

    private static AdvisorAdapterRegistry instance = new DefaultAdvisorAdapterRegistry();

    public static AdvisorAdapterRegistry getInstance() {
        return instance;
    }
}
