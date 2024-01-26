package org.binlog.listener.tactics.impl;

import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import org.binlog.listener.core.BinLogListenerCore;
import org.binlog.listener.tactics.BinLogListener;
import org.binlog.listener.thread.BinLogThreadPool;
import org.binlog.listener.thread.ConsumeThread;
import org.binlog.listener.thread.ProduceThread;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.shyiko.mysql.binlog.event.EventType.*;

/**
 * binlog-format=ROW 时使用的策略
 * @author: JiangWH
 * @date: 2024/1/26 9:30
 * @version: 1.0.0
 */
public class RowTypeBinLogListener implements BinLogListener {
    
    private final ConcurrentHashMap<String, Event> TASK_MAP = new ConcurrentHashMap<>();
    
    private final Lock LOCK = new ReentrantLock();
    
    private final Condition NOT_EMPTY = LOCK.newCondition();
    
    @Override
    public void onEvent(Event event) {
        EventType eventType = event.getHeader().getEventType();
        if(TABLE_MAP == eventType) {
            TableMapEventData tableEventData = event.getData();
            String tableName = tableEventData.getTable();
            if(!BinLogListenerCore.contains(tableName)) {
                //  需要判断是否在监控表集合里面, 不在则跳过
                return;
            }
            
            //  表名等信息, 需要收录到生产者队列中
            BinLogThreadPool.executeTask(new ProduceThread(TASK_MAP, LOCK, NOT_EMPTY, event));
        } else if(isWrite(eventType) || isUpdate(eventType) || isDelete(eventType)) {
            //  增、删、改操作
            BinLogThreadPool.executeTask(new ConsumeThread(TASK_MAP, LOCK, NOT_EMPTY, event, this));
        }
        
    }
    
}
