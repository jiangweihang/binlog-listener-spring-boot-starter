package org.binlog.test;

import org.binlog.listener.annotation.EnableBinlogListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author: JiangWH
 * @date: 2024/1/25 15:42
 * @version: 1.0.0
 */
@EnableBinlogListener
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class BinLogApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BinLogApplication.class, args);
    }
    
}
