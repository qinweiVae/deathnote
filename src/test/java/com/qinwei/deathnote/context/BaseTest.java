package com.qinwei.deathnote.context;

import net.sf.cglib.core.DebuggingClassWriter;
import org.junit.After;
import org.junit.Before;

/**
 * @author qinwei
 * @date 2019-06-21
 */
public class BaseTest {

    AnnotationConfigApplicationContext context;

    @Before
    public void start() {
        // 设置 CGLib 生成 class 的路径
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "D:\\data");
        context = new AnnotationConfigApplicationContext("com.qinwei.deathnote.context.aop");
        //context = new AnnotationConfigApplicationContext(ImportTest.class);
        //context = new AnnotationConfigApplicationContext(Application.class);
        context.start();
    }

    @After
    public void close() {
        context.stop();
        context.close();
    }
}
