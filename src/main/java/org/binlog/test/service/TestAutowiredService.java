package org.binlog.test.service;

import org.springframework.stereotype.Service;

/**
 * @author: JiangWH
 * @date: 2024/1/26 17:43
 * @version: 1.0.0
 */
@Service
public class TestAutowiredService {
    
    public void say() {
        System.out.println("Hello World.");
    }
    
}
