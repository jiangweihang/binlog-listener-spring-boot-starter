package org.binlog.test.service;

import org.binlog.listener.annotation.BinLogEvent;
import org.binlog.listener.annotation.BinLogListener;
import org.binlog.listener.entity.BinLogData;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author: JiangWH
 * @date: 2024/1/25 15:49
 * @version: 1.0.0
 */
@BinLogListener(tableName = "t_a_temp")
public class TestService {
    
    @Autowired
    private TestAutowiredService testAutowiredService;
    
    @BinLogEvent
    public void event(BinLogData dto) {
        System.out.println(dto.toString());
        testAutowiredService.say();
    }
    
    public void haha() {
        testAutowiredService.say();
    }
    
}
