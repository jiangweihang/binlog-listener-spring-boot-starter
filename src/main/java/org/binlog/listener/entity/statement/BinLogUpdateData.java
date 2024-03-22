package org.binlog.listener.entity.statement;

import org.binlog.listener.entity.Where;

import java.util.List;
import java.util.Map;

/**
 * @author: JiangWH
 * @date: 2024/3/22 15:28
 * @version: 1.0.0
 */
public class BinLogUpdateData extends BinLogStatementData {
    
    private Map<String, String> setValues;
    
    private List<Where> wheres;
    
    public BinLogUpdateData() {
    }
    
    public BinLogUpdateData(String sql) {
        this.setSql(sql);
    }
    
    public Map<String, String> getSetValues() {
        return setValues;
    }
    
    public void setSetValues(Map<String, String> setValues) {
        this.setValues = setValues;
    }
    
    public List<Where> getWheres() {
        return wheres;
    }
    
    public void setWheres(List<Where> wheres) {
        this.wheres = wheres;
    }
}
