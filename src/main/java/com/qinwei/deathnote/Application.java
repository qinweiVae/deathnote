package com.qinwei.deathnote;

import com.google.common.annotations.VisibleForTesting;
import com.qinwei.deathnote.config.conf.StandardConfig;
import com.qinwei.deathnote.config.convert.DefaultConversion;
import com.qinwei.deathnote.log.MDCRunnable;
import com.qinwei.deathnote.support.scan.MethodAnnotationScanner;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

/**
 * @author qinwei
 * @date 2019-05-09
 */
@Slf4j
public class Application {

    public static void main(String[] args) {
        MethodAnnotationScanner scanner = new MethodAnnotationScanner();
        Set<Method> classes = scanner.scan(VisibleForTesting.class,"com.google");
        classes.forEach(aClass -> System.out.println(aClass));

        // 入口传入请求ID
        MDC.put("requestId", UUID.randomUUID().toString());

        // 异步线程打印日志，用MDCRunnable装饰Runnable
        new Thread(new MDCRunnable(() -> log.info("log in other thread"))).start();

        new Thread(new MDCRunnable(() -> log.info("log in other thread"))).start();

        // 出口移除请求ID
        log.info("start success ...");
        MDC.remove("requestId");
        StandardConfig config = StandardConfig.getInstance();
        log.info(config.getProperty("java.class.path"));

        DefaultConversion.getInstance();
    }
}
