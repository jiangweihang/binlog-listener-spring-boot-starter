package org.binlog.listener.entity;

import org.binlog.listener.constant.BinLogConstants;

/**
 * @author: JiangWH
 * @date: 2024/1/26 16:09
 * @version: 1.0.0
 */
public class BinLogData {
    
    private String tableName;
    
    private String dbName;
    
    private BinLogConstants.OperatorType type;
    
    public BinLogData() {}
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String getDbName() {
        return dbName;
    }
    
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
    
    public BinLogConstants.OperatorType getType() {
        return type;
    }
    
    public void setType(BinLogConstants.OperatorType type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return "BinLogData{" +
                "tableName='" + tableName + '\'' +
                ", dbName='" + dbName + '\'' +
                ", type=" + type +
                '}';
    }
    
}
