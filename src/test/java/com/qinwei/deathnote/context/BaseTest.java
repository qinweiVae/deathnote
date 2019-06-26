package com.qinwei.deathnote.context;

import com.qinwei.deathnote.context.annotation.Application;
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
        //context = new AnnotationConfigApplicationContext("com.qinwei.deathnote");
        //context = new AnnotationConfigApplicationContext(ImportTest.class);
        context = new AnnotationConfigApplicationContext(Application.class);
        context.start();
    }

    @After
    public void close() {
        context.stop();
        context.close();
    }
}
