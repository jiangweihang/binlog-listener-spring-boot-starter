package org.binlog.listener.entity.statement;

import org.binlog.listener.entity.Where;

import java.util.List;
import java.util.Map;

/**
 * @author: JiangWH
 * @date: 2024/3/22 15:28
 * @version: 1.0.0
 */
public class BinLogDeleteData extends BinLogStatementData {
    
    private List<Where> wheres;
    
    public BinLogDeleteData() {
    }
    
    public BinLogDeleteData(String sql) {
        this.setSql(sql);
    }
    
    public List<Where> getWheres() {
        return wheres;
    }
    
    public void setWheres(List<Where> wheres) {
        this.wheres = wheres;
    }
    
}
