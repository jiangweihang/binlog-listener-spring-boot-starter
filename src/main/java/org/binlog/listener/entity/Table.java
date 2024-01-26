package org.binlog.listener.entity;

import java.util.List;

/**
 * @author: JiangWH
 * @date: 2024/1/26 9:34
 * @version: 1.0.0
 */
public class Table {
    
    private String dbName;
    
    private String tableName;
    
    private List<Column> columns;
    
    public String getDbName() {
        return dbName;
    }
    
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public List<Column> getColumns() {
        return columns;
    }
    
    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
    
}
