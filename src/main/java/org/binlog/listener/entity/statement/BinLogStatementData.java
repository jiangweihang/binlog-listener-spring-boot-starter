package org.binlog.listener.entity.statement;

import org.binlog.listener.entity.BinLogData;

/**
 * @author: JiangWH
 * @date: 2024/3/22 15:28
 * @version: 1.0.0
 */
public class BinLogStatementData extends BinLogData {
    
    private String sql;
    
    public String getSql() {
        return sql;
    }
    
    public void setSql(String sql) {
        this.sql = sql;
    }
    
}
