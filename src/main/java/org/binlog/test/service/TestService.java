package org.binlog.test.service;

import com.alibaba.fastjson.JSONObject;
import org.binlog.listener.annotation.BinLogEvent;
import org.binlog.listener.annotation.BinLogListener;
import org.binlog.listener.entity.BinLogData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: JiangWH
 * @date: 2024/1/25 15:49
 * @version: 1.0.0
 */
@BinLogListener(tableName = "t_user")
public class TestService {

    @Autowired
    private TestByTableNameService testByTableNameService;
    
    @BinLogEvent
    public void event(BinLogData dto) {
        System.out.println("t_user这个是没有指定数据库的: " + JSONObject.toJSONString(dto));
    }

    @Transactional
    public void test() {
        System.out.println("这是事务处理");
    }
    
}
