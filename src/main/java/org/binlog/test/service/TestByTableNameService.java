package org.binlog.test.service;

import com.alibaba.fastjson.JSONObject;
import org.binlog.listener.annotation.BinLogEvent;
import org.binlog.listener.annotation.BinLogListener;
import org.binlog.listener.entity.BinLogData;

/**
 * @author: JiangWH
 * @date: 2024/1/25 15:49
 * @version: 1.0.0
 */
@BinLogListener(tableName = "t_user_copy1")
public class TestByTableNameService {
    
    @BinLogEvent
    public void event(BinLogData dto) {
        System.out.println("t_user_copy1: " + JSONObject.parseObject(JSONObject.toJSONString(dto)).getJSONArray("data").getJSONObject(0).getString("name"));
    }
    
}
