package org.binlog.test;

import org.binlog.listener.annotation.EnableBinlogListener;
import org.binlog.test.service.TestByDbNameService;
import org.binlog.test.service.TestService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @Author: Emiya
 * @Date: 2026/4/3 21:39
 * @Description: test
 */
@EnableBinlogListener
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(TestApplication.class, args);
        TestByDbNameService bean = run.getBean(TestByDbNameService.class);
        TestService bean1 = run.getBean(TestService.class);
        System.out.println(bean);
        System.out.println(bean1);
    }
}
