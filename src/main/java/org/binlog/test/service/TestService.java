package org.binlog.test.service;

import com.alibaba.fastjson.JSONObject;
import org.binlog.listener.annotation.BinLogEvent;
import org.binlog.listener.annotation.BinLogListener;
import org.binlog.listener.entity.BinLogDataDto;
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
    public void event(BinLogDataDto dto) {
        System.out.println(JSONObject.toJSONString(dto));
        testAutowiredService.say();
    }
    
    public void haha() {
        testAutowiredService.say();
    }
    
}
