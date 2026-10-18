package org.binlog.test.service;

import com.alibaba.fastjson.JSONObject;
import org.binlog.listener.annotation.BinLogEvent;
import org.binlog.listener.annotation.BinLogListener;
import org.binlog.listener.constant.BinLogConstants;
import org.binlog.listener.entity.BinLogData;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

/**
 * @author: JiangWH
 * @date: 2024/1/25 15:49
 * @version: 1.0.0
 */
@BinLogListener(tableName = "t_user", dbName = "test_anything")
public class TestByDbNameService {

    private final TestService testService;

    public TestByDbNameService(TestService testService) {
        this.testService = testService;
    }

    @PostConstruct
    public void init() {
        System.out.println("TestByDbNameService 初始化完成");
    }
    
    @BinLogEvent(callbackType = BinLogConstants.CallbackType.SINGLE)
    public void event(BinLogData dto) throws InterruptedException {
//        System.out.println("t_user: " + JSONObject.parseObject(JSONObject.toJSONString(dto)).getJSONArray("data").getJSONObject(0).getInteger("age"));
        Thread.sleep(20000L);
        System.out.println(JSONObject.toJSONString(dto));
    }

    @Transactional
    public void test() {
        System.out.println("这是事务处理");
    }
    
}
