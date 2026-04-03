package org.binlog.listener.tactics.impl;

import com.github.shyiko.mysql.binlog.event.*;
import org.binlog.listener.constant.BinLogConstants;
import org.binlog.listener.core.BinLogListenerCore;
import org.binlog.listener.entity.Column;
import org.binlog.listener.entity.row.BinLogRowData;
import org.binlog.listener.tactics.BinLogListener;
import org.binlog.listener.thread.BinLogThreadPool;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.github.shyiko.mysql.binlog.event.EventType.*;

/**
 * binlog-format=ROW 时使用的策略
 * @author: JiangWH
 * @date: 2024/1/26 9:30
 * @version: 1.0.0
 */
public class RowTypeBinLogListener implements BinLogListener {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 保存当前的 TABLE_MAP 事件，用于后续数据事件处理
     * BinaryLogClient 是单线程顺序回调，无需同步
     */
    private Event currentTableEvent;

    @Override
    public void onEvent(Event event) {
        EventType eventType = event.getHeader().getEventType();
        if(EventType.TABLE_MAP == eventType) {
            TableMapEventData tableEventData = event.getData();
            String tableName = tableEventData.getTable();
            if(!BinLogListenerCore.contains(tableName)) {
                //  不在监控表集合里面，跳过并清除缓存
                currentTableEvent = null;
                return;
            }
            //  保存 TABLE_MAP 事件
            currentTableEvent = event;
        } else if(isWrite(eventType) || isUpdate(eventType) || isDelete(eventType)) {
            //  增、删、改操作，直接使用缓存的 TABLE_MAP 事件处理
            if(currentTableEvent != null) {
                initData(currentTableEvent, event);
            }
        }
    }

    /**
     * 处理数据
     * @param tableEvent  数据库名、表名
     * @param dataEvent  列信息、数据信息
     */
    private void initData(Event tableEvent, Event dataEvent) {
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
            throw new RuntimeException("Unknown type of action.");
        }

        BinLogRowData dto = new BinLogRowData(tableName, database, type, columnList, rowList, updateBeforeList);
        BinLogThreadPool.executeTask(() -> BinLogListenerCore.run(tableName, database, dto));
    }

    private void updateType(UpdateRowsEventData data, List<Column> columnList, List<Map<String, String>> rowList, List<Map<String, String>> updateBeforeList) {
        List<Map.Entry<Serializable[], Serializable[]>> rows = data.getRows();
        for (Map.Entry<Serializable[], Serializable[]> row : rows) {
            addRows(new ArrayList<Serializable[]>() {{add(row.getKey());}}, columnList, updateBeforeList);
            addRows(new ArrayList<Serializable[]>() {{add(row.getValue());}}, columnList, rowList);
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
            return DATE_FORMATTER.format(((Date) item).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        return null;
    }

}
