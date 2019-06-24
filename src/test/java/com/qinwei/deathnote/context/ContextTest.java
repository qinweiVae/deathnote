package com.qinwei.deathnote.context;

import com.qinwei.deathnote.support.spi.Worker;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author qinwei
 * @date 2019-06-21
 */
@Slf4j
public class ContextTest extends BaseTest {

    @Test
    public void testApplicationContext() {
        String[] beanDefinitionNames = context.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {

            log.info(beanDefinitionName);
        }
        log.info("-----------------------------------");
        for (String name : context.getBeanNamesForType(Worker.class)) {
            log.info(name);
        }
    }
}
