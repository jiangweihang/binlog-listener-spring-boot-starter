package org.binlog.listener.entity.row;

import org.binlog.listener.constant.BinLogConstants;
import org.binlog.listener.entity.BinLogData;
import org.binlog.listener.entity.Column;

import java.util.List;
import java.util.Map;

/**
 * @author: JiangWH
 * @date: 2024/1/26 16:09
 * @version: 1.0.0
 */
public class BinLogRowData extends BinLogData {
    
    private List<Column> columns;
    
    private List<Map<String, String>> data;
    
    private List<Map<String, String>> updateBefore;
    
    public BinLogRowData() {}
    
    public BinLogRowData(String tableName, String dbName, BinLogConstants.OperatorType type, List<Column> columns,
                         List<Map<String, String>> data, List<Map<String, String>> updateBefore) {
        this.setTableName(tableName);
        this.setDbName(dbName);
        this.setType(type);
        this.columns = columns;
        this.data = data;
        this.updateBefore = updateBefore;
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
