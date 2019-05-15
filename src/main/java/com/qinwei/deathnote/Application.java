package com.qinwei.deathnote;

import com.qinwei.deathnote.config.conf.StandardConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Scanner;

/**
 * @author qinwei
 * @date 2019-05-09
 */
@Slf4j
public class Application {

    public static void main(String[] args) {
       /* MethodAnnotationScanner scanner = new MethodAnnotationScanner();
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

        DefaultConversion.getInstance();*/
        System.setProperty("config.path", "d:/config");
        System.setProperty("qinwei.date", "2019-05-14 14:09:20");
        System.out.println(StandardConfig.getInstance().getProperty("qinwei.date", Date.class, null));
        System.out.println(StandardConfig.getInstance().getProperty("author"));
        Scanner scanner = new Scanner(System.in);
        while (!scanner.hasNextInt()) {
            System.out.println(StandardConfig.getInstance().getProperty(scanner.nextLine()));
        }
        scanner.close();
    }
}
