package com.qinwei.deathnote;

import com.qinwei.deathnote.log.MDCRunnable;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * @author qinwei
 * @date 2019-05-09
 */
@Slf4j
public class Application {

    public static void main(String[] args) {

        // 入口传入请求ID
        MDC.put("requestId", UUID.randomUUID().toString());

        // 异步线程打印日志，用MDCRunnable装饰Runnable
        new Thread(new MDCRunnable(() -> log.info("log in other thread"))).start();

        new Thread(new MDCRunnable(() -> log.info("log in other thread"))).start();

        // 出口移除请求ID
        log.info("start success ...");
        MDC.remove("requestId");
    }
}
