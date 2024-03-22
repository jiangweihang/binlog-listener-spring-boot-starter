package org.binlog.listener.core;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import org.binlog.listener.entity.BinLogData;
import org.binlog.listener.entity.Where;
import org.binlog.listener.entity.statement.BinLogDeleteData;
import org.binlog.listener.entity.statement.BinLogInsertData;
import org.binlog.listener.entity.statement.BinLogUpdateData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: JiangWH
 * @date: 2024/3/22 14:11
 * @version: 1.0.0
 */
public class DruidParser {
    
    public static BinLogData update(String sql) {
        BinLogUpdateData result = new BinLogUpdateData(sql);
        SQLUpdateStatement updateStatement = (SQLUpdateStatement) SQLUtils.parseSingleMysqlStatement(sql);
        //  获取表名
        result.setTableName(updateStatement.getTableName().getSimpleName());
        
        //  获取更新设置的值
        Map<String, String> setValues = new HashMap<>();
        for (SQLUpdateSetItem item : updateStatement.getItems()) {
            setValues.put(item.getColumn().toString(), item.getValue().toString());
        }
        
        //  获取更新条件
        List<Where> wheres = parseWhere(updateStatement.getWhere());
        result.setSetValues(setValues);
        result.setWheres(wheres);
        return result;
    }
    
    public static BinLogData insert(String sql) {
        BinLogInsertData result = new BinLogInsertData(sql);
        SQLInsertStatement insertStatement = (SQLInsertStatement) SQLUtils.parseSingleMysqlStatement(sql);
        //  获取表名
        result.setTableName(insertStatement.getTableName().getSimpleName());
        
        //  是否有选择字段赋值
        List<SQLExpr> columns = insertStatement.getColumns();
        if(columns == null || columns.isEmpty()) {
            result.setAllColumn(true);
        } else {
            //  获取选择字段
            List<String> resultColumns = new ArrayList<>();
            for (SQLExpr column : columns) {
                resultColumns.add(column.toString());
            }
            result.setColumns(resultColumns);
        }
        
        //  获取新增的值
        List<List<String>> resultValues = new ArrayList<>();
        List<SQLInsertStatement.ValuesClause> valuesList = insertStatement.getValuesList();
        for (SQLInsertStatement.ValuesClause valuesClause : valuesList) {
            List<String> row = new ArrayList<>();
            for (SQLExpr value : valuesClause.getValues()) {
                row.add(value.toString());
            }
            resultValues.add(row);
        }
        result.setValuesClause(resultValues);
        
        return result;
    }
    
    public static BinLogData delete(String sql) {
        BinLogDeleteData result = new BinLogDeleteData(sql);
        SQLDeleteStatement deleteStatement = (SQLDeleteStatement) SQLUtils.parseSingleMysqlStatement(sql);
        result.setTableName(deleteStatement.getTableName().getSimpleName());
        List<Where> wheres = parseWhere(deleteStatement.getWhere());
        result.setWheres(wheres);
        return result;
    }
    
    private static List<Where> parseWhere(SQLExpr where) {
        List<Where> wheres = new ArrayList<>();
        parseWhere(where.getChildren(), wheres);
        return wheres;
    }
    
    private static void parseWhere(List<SQLObject> children, List<Where> wheres) {
        if(children == null || children.isEmpty()) {
            return;
        }
        for (SQLObject child : children) {
            SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) child;
            if(binaryOpExpr.getOperator().equals(SQLBinaryOperator.BooleanAnd)) {
                parseWhere(binaryOpExpr.getChildren(), wheres);
            } else {
                wheres.add(new Where(binaryOpExpr.getLeft().toString(), binaryOpExpr.getOperator(), binaryOpExpr.getRight().toString()));
            }
        }
    }
    
}
