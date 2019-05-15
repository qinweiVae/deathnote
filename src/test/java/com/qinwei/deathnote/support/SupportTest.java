package com.qinwei.deathnote.support;

import com.qinwei.deathnote.config.conf.StandardConfig;
import com.qinwei.deathnote.support.scan.ResourcesScanner;
import org.junit.Test;

import java.util.Date;
import java.util.Scanner;

/**
 * @author qinwei
 * @date 2019-05-14
 */
public class SupportTest {

    @Test
    public void testConfig() {
        System.setProperty("config.path", "d:/config");
        System.setProperty("qinwei.date", "2019-05-14 14:09:20");
        System.out.println(StandardConfig.getInstance().getProperty("qinwei.date", Date.class, null));
        System.out.println(StandardConfig.getInstance().getProperty("author"));
        Scanner scanner = new Scanner(System.in);
        System.out.println(StandardConfig.getInstance().getProperty(scanner.nextLine()));
    }

    @Test
    public void testScan() {
        System.setProperty("config.path", "d:/config/config-test.properties");
        ResourcesScanner scanner = ResourcesScanner.getInstance();
        scanner.scan().forEach((s, s2) -> System.out.println(s + ":" + s2));
    }
}
