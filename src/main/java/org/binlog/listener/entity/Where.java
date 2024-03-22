package org.binlog.listener.entity;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;

/**
 * @author: JiangWH
 * @date: 2024/3/22 15:39
 * @version: 1.0.0
 */
public class Where {
    
    private String column;
    
    private SQLBinaryOperator operator;
    
    private String value;
    
    public Where() {
    }
    
    public Where(String column, SQLBinaryOperator operator, String value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }
    
    public String getColumn() {
        return column;
    }
    
    public void setColumn(String column) {
        this.column = column;
    }
    
    public SQLBinaryOperator getOperator() {
        return operator;
    }
    
    public void setOperator(SQLBinaryOperator operator) {
        this.operator = operator;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
}
