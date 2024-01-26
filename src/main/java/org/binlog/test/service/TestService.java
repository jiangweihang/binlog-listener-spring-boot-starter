package org.binlog.test.service;

import com.alibaba.fastjson.JSONObject;
import org.binlog.listener.annotation.BinLogEvent;
import org.binlog.listener.annotation.BinLogListener;
import org.binlog.listener.entity.BinLogDataDto;

/**
 * @author: JiangWH
 * @date: 2024/1/25 15:49
 * @version: 1.0.0
 */
@BinLogListener(tableName = "t_a_temp")
public class TestService {
    
    @BinLogEvent
    public void event(BinLogDataDto dto) {
        System.out.println(JSONObject.toJSONString(dto));
    }
    
    public void haha() {
        System.out.println("hahaha");
    }
    
}
