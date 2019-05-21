package com.qinwei.deathnote.support;

import com.google.common.annotations.VisibleForTesting;
import com.qinwei.deathnote.config.conf.Config;
import com.qinwei.deathnote.config.conf.StandardConfig;
import com.qinwei.deathnote.log.MDCRunnable;
import com.qinwei.deathnote.support.scan.MethodAnnotationScanner;
import com.qinwei.deathnote.support.scan.ResourcesScanner;
import com.qinwei.deathnote.support.spi.ServiceLoader;
import com.qinwei.deathnote.support.spi.Worker;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.slf4j.MDC;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static com.qinwei.deathnote.support.scan.ResourcesScanner.CONFIG_PATH;

/**
 * @author qinwei
 * @date 2019-05-14
 */
@Slf4j
public class SupportTest {

    @Test
    public void testConfig() {
        System.setProperty(CONFIG_PATH, "d:/config");
        System.setProperty("qinwei.date", "2019-05-14 14:09:20");
        Config config = StandardConfig.getInstance();
        config.initConfig();
        System.out.println(config.getProperty("qinwei.date", Date.class, new Date()));
        System.out.println(config.getProperty("author"));
    }

    @Test
    public void testScan() {
        System.setProperty(CONFIG_PATH, "d:/config/config-test.properties");
        ResourcesScanner scanner = ResourcesScanner.getInstance();
        scanner.scan().forEach((s, s2) -> System.out.println(s + ":" + s2));
        MethodAnnotationScanner methodAnnotationScanner = new MethodAnnotationScanner();
        Set<Method> classes = methodAnnotationScanner.scan(VisibleForTesting.class, "com.google");
        classes.forEach(aClass -> System.out.println(aClass));
    }

    @Test
    public void testLog() {
        // 入口传入请求ID
        MDC.put("requestId", UUID.randomUUID().toString());

        // 异步线程打印日志，用MDCRunnable装饰Runnable
        new Thread(new MDCRunnable(() -> log.info("log in other thread"))).start();

        new Thread(new MDCRunnable(() -> log.info("log in other thread"))).start();

        // 出口移除请求ID
        log.info("start success ...");
        MDC.remove("requestId");
    }

    @Test
    public void testSpi() {
        Worker worker = ServiceLoader.getService(Worker.class);
        worker.work();
        System.out.println(worker == ServiceLoader.getService(Worker.class, false));
        Worker female = ServiceLoader.getService(Worker.class, "female");
        female.work();
        System.out.println(female == ServiceLoader.getService(Worker.class, "female"));
        System.out.println(female == ServiceLoader.getService(Worker.class, "female", false));
        Worker child = ServiceLoader.getService(Worker.class, "child");
        child.work();
    }
}
