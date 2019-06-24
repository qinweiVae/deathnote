package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.support.spi.ChildWorker;
import com.qinwei.deathnote.support.spi.FemaleWorker;
import com.qinwei.deathnote.support.spi.MaleWorker;
import com.qinwei.deathnote.support.spi.Worker;

/**
 * @author qinwei
 * @date 2019-06-24
 */
@ComponentScan(basePackages = "com.qinwei.deathnote", excludeFilters = Component.class)
@Configuration
public class Application {

    @Bean
    public Worker maleWorker() {
        return new MaleWorker();
    }

    @Bean
    public Worker femaleWorker() {
        return new FemaleWorker();
    }

    @Bean
    public Worker childWorker() {
        return new ChildWorker();
    }
}
