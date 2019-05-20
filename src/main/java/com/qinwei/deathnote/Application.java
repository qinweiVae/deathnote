package com.qinwei.deathnote;

import com.qinwei.deathnote.config.conf.StandardConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

import static com.qinwei.deathnote.support.scan.ResourcesScanner.CONFIG_PATH;

/**
 * @author qinwei
 * @date 2019-05-09
 */
@Slf4j
public class Application {

    public static void main(String[] args) {
        System.setProperty(CONFIG_PATH, "d:/config");
        Scanner scanner = new Scanner(System.in);
        while (!scanner.hasNextInt()) {
            System.out.println(StandardConfig.getInstance().getProperty(scanner.nextLine()));
        }
        scanner.close();
    }
}
