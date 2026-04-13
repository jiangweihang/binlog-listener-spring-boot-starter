package org.binlog.test.service;

import com.alibaba.fastjson.JSONObject;
import org.binlog.listener.annotation.BinLogEvent;
import org.binlog.listener.annotation.BinLogListener;
import org.binlog.listener.constant.BinLogConstants;
import org.binlog.listener.entity.BinLogData;

/**
 * @author: JiangWH
 * @date: 2024/1/25 15:49
 * @version: 1.0.0
 */
@BinLogListener(tableName = "t_user", dbName = "test_anything")
public class TestByDbNameService {
    
    @BinLogEvent(callbackType = BinLogConstants.CallbackType.SINGLE)
    public void event(BinLogData dto) throws InterruptedException {
//        System.out.println("t_user: " + JSONObject.parseObject(JSONObject.toJSONString(dto)).getJSONArray("data").getJSONObject(0).getInteger("age"));
        Thread.sleep(2000L);
        System.out.println(JSONObject.toJSONString(dto));
    }
    
}
