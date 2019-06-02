package com.qinwei.deathnote;

import com.qinwei.deathnote.config.Config;
import com.qinwei.deathnote.config.StandardConfig;
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
        Config config = StandardConfig.getInstance();
        config.initConfig();
        Scanner scanner = new Scanner(System.in);
        while (!scanner.hasNextInt()) {
            System.out.println(config.getProperty(scanner.nextLine()));
        }
        scanner.close();
    }
}
