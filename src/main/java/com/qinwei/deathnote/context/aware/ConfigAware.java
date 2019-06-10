package com.qinwei.deathnote.context.aware;

import com.qinwei.deathnote.config.Config;

/**
 * @author qinwei
 * @date 2019-06-10
 */
public interface ConfigAware {

    void setConfig(Config config);
}
