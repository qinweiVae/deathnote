package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.support.spi.ChildWorker;
import com.qinwei.deathnote.support.spi.FemaleWorker;
import com.qinwei.deathnote.support.spi.MaleWorker;
import com.qinwei.deathnote.support.spi.Worker;

/**
 * @author qinwei
 * @date 2019-06-24
 */
@ComponentScan(basePackages = "com.qinwei.deathnote")
@Configuration
public class Application {

    @Bean(value = "male", initMethod = "work", destroyMethod = "stop")
    public Worker maleWorker() {
        MaleWorker maleWorker = new MaleWorker();
        maleWorker.setName("qw");
        return maleWorker;
    }

    @Bean(initMethod = "work", destroyMethod = "stop")
    @Primary
    //@Lazy
    public Worker primaryWorker() {
        MaleWorker maleWorker = new MaleWorker();
        maleWorker.setName("primary");
        return maleWorker;
    }

    @Bean(value = "female", initMethod = "work", destroyMethod = "stop")
    @DependsOn("child")
    public Worker femaleWorker(MaleWorker maleWorker) {
        FemaleWorker femaleWorker = new FemaleWorker();
        femaleWorker.setName(maleWorker.getName());
        return femaleWorker;
    }

    @Bean(value = "child", initMethod = "work", destroyMethod = "stop")
    @DependsOn("male")
    public Worker childWorker(MaleWorker maleWorker) {
        ChildWorker childWorker = new ChildWorker();
        childWorker.setName(maleWorker.getName());
        return childWorker;
    }
}
