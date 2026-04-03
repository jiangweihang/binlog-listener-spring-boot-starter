package org.binlog.listener.tactics.impl;

import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.QueryEventData;
import org.binlog.listener.constant.BinLogConstants;
import org.binlog.listener.core.BinLogListenerCore;
import org.binlog.listener.core.DruidParser;
import org.binlog.listener.entity.BinLogData;
import org.binlog.listener.tactics.BinLogListener;
import org.binlog.listener.thread.BinLogThreadPool;

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
        EventData data = event.getData();
        if(!(data instanceof QueryEventData)) {
            return;
        }
        QueryEventData eventData = (QueryEventData) data;
        if(eventData.getDatabase() == null || eventData.getDatabase().isEmpty()) {
            return;
        }
    
        String sql = eventData.getSql();
        BinLogConstants.OperatorType type = null;
        BinLogData binLogData;
        if(sql.toUpperCase().startsWith(BinLogConstants.OperatorType.INSERT.toString())) {
            type = BinLogConstants.OperatorType.INSERT;
            binLogData = DruidParser.insert(sql);
        } else if(sql.toUpperCase().startsWith(BinLogConstants.OperatorType.UPDATE.toString())) {
            type = BinLogConstants.OperatorType.UPDATE;
            binLogData = DruidParser.update(sql);
        } else if(sql.toUpperCase().startsWith(BinLogConstants.OperatorType.DELETE.toString())) {
            type = BinLogConstants.OperatorType.DELETE;
            binLogData = DruidParser.delete(sql);
        } else {
            binLogData = null;
            throw new RuntimeException("Unknown type of action.");
        }
        binLogData.setDbName(eventData.getDatabase());
        binLogData.setType(type);
        binLogData.setTableName(binLogData.getTableName().replaceAll("`", ""));

        BinLogThreadPool.executeTask(new Thread(() -> BinLogListenerCore.run(binLogData.getTableName(), binLogData.getDbName(), binLogData)));
    }
    
}
