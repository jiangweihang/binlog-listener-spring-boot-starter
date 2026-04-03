package org.binlog.test;

import org.binlog.listener.annotation.EnableBinlogListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: Emiya
 * @Date: 2026/4/3 21:39
 * @Description: test
 */
@EnableBinlogListener
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
