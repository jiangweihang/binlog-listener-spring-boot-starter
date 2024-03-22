package org.binlog.listener.thread.row;

import com.github.shyiko.mysql.binlog.event.*;
import org.binlog.listener.constant.BinLogConstants;
import org.binlog.listener.core.BinLogListenerCore;
import org.binlog.listener.entity.row.BinLogRowData;
import org.binlog.listener.entity.Column;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.github.shyiko.mysql.binlog.event.EventType.*;

/**
 * binlog-format=ROW 时使用的格式化数据线程
 * @author: JiangWH
 * @date: 2024/1/26 15:05
 * @version: 1.0.0
 */
public class CoreRowThread implements Runnable {
    
    private final Event tableEvent;
    
    private final Event dataEvent;
    
    public CoreRowThread(Event tableEvent, Event dataEvent) {
        this.tableEvent = tableEvent;
        this.dataEvent = dataEvent;
    }
    
    @Override
    public void run() {
        TableMapEventData tableEventData = tableEvent.getData();
        String tableName = tableEventData.getTable();
        String database = tableEventData.getDatabase();
    
        List<Map<String, String>> rowList = new ArrayList<>(), updateBeforeList = new ArrayList<>();
        List<Column> columnList = BinLogListenerCore.getColumn(tableName, database);
        //  判断操作类型
        EventType eventType = dataEvent.getHeader().getEventType();
        BinLogConstants.OperatorType type = null;
        if(isWrite(eventType)) {
            type = BinLogConstants.OperatorType.INSERT;
            writeType(dataEvent.getData(), columnList, rowList);
        } else if (isDelete(eventType)) {
            type = BinLogConstants.OperatorType.DELETE;
            deleteType(dataEvent.getData(), columnList, rowList);
        } else if (isUpdate(eventType)) {
            type = BinLogConstants.OperatorType.UPDATE;
            updateType(dataEvent.getData(), columnList, rowList, updateBeforeList);
        } else {
            throw new RuntimeException("未知的操作类型.");
        }
        
        BinLogRowData dto = new BinLogRowData(tableName, database, type, columnList, rowList, updateBeforeList);
        BinLogListenerCore.run(tableName, database, dto);
    }
    
    private void updateType(UpdateRowsEventData data, List<Column> columnList, List<Map<String, String>> rowList, List<Map<String, String>> updateBeforeList) {
        List<Map.Entry<Serializable[], Serializable[]>> rows = data.getRows();
        for (Map.Entry<Serializable[], Serializable[]> row : rows) {
            addRows(new ArrayList<Serializable[]>() {{add(row.getKey());}}, columnList, rowList);
            addRows(new ArrayList<Serializable[]>() {{add(row.getValue());}}, columnList, updateBeforeList);
        }
    }
    
    private void deleteType(DeleteRowsEventData data, List<Column> columnList, List<Map<String, String>> rowList) {
        List<Serializable[]> rows = data.getRows();
        addRows(rows, columnList, rowList);
    }
    
    private void writeType(WriteRowsEventData data, List<Column> columnList, List<Map<String, String>> rowList) {
        List<Serializable[]> rows = data.getRows();
        addRows(rows, columnList, rowList);
    }
    
    private void addRows(List<Serializable[]> rows, List<Column> columnList, List<Map<String, String>> rowList) {
        for (Serializable[] row : rows) {
            Map<String, String> item = new HashMap<>();
            for (Column column : columnList) {
                Serializable serializable = row[column.getPosition() - 1];
                item.put(column.getName(), convertToStr(serializable));
            }
            rowList.add(item);
        }
    }
    
    /**
     * 转换Sql字符串
     *
     * @param item 参数
     * @return {@link java.lang.String}
     * @author nza
     * @createTime 2020/12/22 13:21
     */
    private String convertToStr(Object item) {
        if (item == null) {
            return null;
        }
        if(item instanceof byte[]) {
            try {
                return new String((byte[]) item, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (BinLogConstants.NORMAL_TYPE.contains(item.getClass())) {
            return String.valueOf(item);
        }
        if (item instanceof Boolean) {
            return (boolean) item ? String.valueOf(1) : String.valueOf(0);
        }
        if (item instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item);
        }
        return null;
    }
    
}
