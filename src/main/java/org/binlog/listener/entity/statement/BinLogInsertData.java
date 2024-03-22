package org.binlog.listener.entity.statement;

import java.util.List;

/**
 * @author: JiangWH
 * @date: 2024/3/22 15:28
 * @version: 1.0.0
 */
public class BinLogInsertData extends BinLogStatementData {
    
    private Boolean allColumn;
    
    private List<String> columns;
    
    private List<List<String>> valuesClause;
    
    public BinLogInsertData() {
    }
    
    public BinLogInsertData(String sql) {
        this.setSql(sql);
    }
    
    public Boolean getAllColumn() {
        return allColumn;
    }
    
    public void setAllColumn(Boolean allColumn) {
        this.allColumn = allColumn;
    }
    
    public List<String> getColumns() {
        return columns;
    }
    
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
    
    public List<List<String>> getValuesClause() {
        return valuesClause;
    }
    
    public void setValuesClause(List<List<String>> valuesClause) {
        this.valuesClause = valuesClause;
    }
}
