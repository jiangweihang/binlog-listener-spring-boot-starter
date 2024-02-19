package org.binlog.listener.tactics.impl;

import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.QueryEventData;
import org.binlog.listener.constant.BinLogConstants;
import org.binlog.listener.tactics.BinLogListener;

/**
 * binlog-format=STATEMENT 时使用的策略
 * 现在只返回执行的SQL语句, 还没想好怎么合理处理STATEMENT格式的事件
 * @author: JiangWH
 * @date: 2024/1/26 9:30
 * @version: 1.0.0
 */
public class StatementTypeBinLogListener implements BinLogListener {
    
    @Override
    public void onEvent(Event event) {
        QueryEventData eventData = event.getData();
        if(eventData == null || eventData.getDatabase() == null || eventData.getDatabase().isEmpty()) {
            return;
        }
        System.out.println(eventData.getDatabase());
        System.out.println(eventData.getSql());
    
        String sql = eventData.getSql();
        String tableName = null;
        BinLogConstants.OperatorType type = null;
        if(sql.toUpperCase().startsWith(BinLogConstants.OperatorType.INSERT.toString())) {
            type = BinLogConstants.OperatorType.INSERT;
        } else if(sql.toUpperCase().startsWith(BinLogConstants.OperatorType.UPDATE.toString())) {
            type = BinLogConstants.OperatorType.UPDATE;
        } else if(sql.toUpperCase().startsWith(BinLogConstants.OperatorType.DELETE.toString())) {
            type = BinLogConstants.OperatorType.DELETE;
        } else {
            throw new RuntimeException("未知的操作类型.");
        }
        
//        if(TABLE_MAP == eventType) {
//            TableMapEventData tableEventData = event.getData();
//            String tableName = tableEventData.getTable();
//            if(!BinLogListenerCore.contains(tableName)) {
//                //  需要判断是否在监控表集合里面, 不在则跳过
//                return;
//            }
//
//            //  表名等信息, 需要收录到生产者队列中
//            BinLogThreadPool.executeTask(new ProduceThread(TASK_MAP, LOCK, NOT_EMPTY, event));
//        } else if(isWrite(eventType) || isUpdate(eventType) || isDelete(eventType)) {
//            //  增、删、改操作
//            BinLogThreadPool.executeTask(new ConsumeThread(TASK_MAP, LOCK, NOT_EMPTY, event, this));
//        }
    
    }
    
}
