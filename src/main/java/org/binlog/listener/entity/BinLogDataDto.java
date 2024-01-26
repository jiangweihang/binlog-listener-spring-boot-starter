package org.binlog.listener.entity;

import org.binlog.listener.constant.BinLogConstants;

import java.util.List;
import java.util.Map;

/**
 * @author: JiangWH
 * @date: 2024/1/26 16:09
 * @version: 1.0.0
 */
public class BinLogDataDto {
    
    private String tableName;
    
    private String dbName;
    
    private BinLogConstants.OperatorType type;
    
    private List<Column> columns;
    
    private List<Map<String, String>> data;
    
    private List<Map<String, String>> updateBefore;
    
    public BinLogDataDto() {}
    
    public BinLogDataDto(String tableName, String dbName, BinLogConstants.OperatorType type, List<Column> columns,
                         List<Map<String, String>> data, List<Map<String, String>> updateBefore) {
        this.tableName = tableName;
        this.dbName = dbName;
        this.type = type;
        this.columns = columns;
        this.data = data;
        this.updateBefore = updateBefore;
    }
    
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
    
    public List<Column> getColumns() {
        return columns;
    }
    
    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
    
    public List<Map<String, String>> getData() {
        return data;
    }
    
    public void setData(List<Map<String, String>> data) {
        this.data = data;
    }
    
    public List<Map<String, String>> getUpdateBefore() {
        return updateBefore;
    }
    
    public void setUpdateBefore(List<Map<String, String>> updateBefore) {
        this.updateBefore = updateBefore;
    }
    
}
