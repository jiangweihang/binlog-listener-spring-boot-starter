package org.binlog.listener.core;

import org.binlog.listener.cglib.BinLogServiceProxy;
import org.binlog.listener.entity.Column;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: JiangWH
 * @date: 2024/1/25 16:46
 * @version: 1.0.0
 */
public class BinLogListenerCore {
    
    private final static Map<String, BinLogServiceProxy> PROXY_MAP = new HashMap<>();
    
    /**
     * 第一个Map的key是表名，value的Key是数据库名, value的value是列集合
     */
    private final static Map<String, Map<String, List<Column>>> COLUMN_MAP = new HashMap<>();
    
    private BinLogListenerCore() {}
    
    public static List<Column> getColumn(String tableKey, String dbKey) {
        return COLUMN_MAP.get(tableKey).get(dbKey);
    }
    
    public static void putColumn(String tableKey, String dbKey, List<Column> columnList) {
        Map<String, List<Column>> innerMap = COLUMN_MAP.get(tableKey);
        if(innerMap == null) {
            innerMap = new HashMap<>();
        }
        innerMap.put(dbKey, columnList);
        COLUMN_MAP.put(tableKey, innerMap);
    }
    
    public static void put(String key, BinLogServiceProxy value) {
        PROXY_MAP.put(key, value);
    }
    
    public static boolean contains(String key) {
        return PROXY_MAP.containsKey(key);
    }
    
    public static BinLogServiceProxy get(String key) {
        return PROXY_MAP.get(key);
    }
    
    public static Set<String> getAllKey() {
        return PROXY_MAP.keySet();
    }
    
    public static void run(String key, Object data) {
        BinLogServiceProxy binLogServiceProxy = get(key);
        if(binLogServiceProxy == null) {
            return;
        }
        try {
            binLogServiceProxy.intercept(null, null, new Object[]{data}, null);
        } catch (Throwable e) {}
    }
    
}
