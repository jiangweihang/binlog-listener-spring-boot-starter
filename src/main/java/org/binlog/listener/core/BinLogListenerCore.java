package org.binlog.listener.core;

import org.binlog.listener.cglib.BinLogProxy;
import org.binlog.listener.cglib.BinLogServiceProxy;
import org.binlog.listener.entity.Column;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: JiangWH
 * @date: 2024/1/25 16:46
 * @version: 1.0.0
 */
public class BinLogListenerCore {

    /**
     * 用来初始化 {@link org.binlog.listener.core.BinLogListenerCore#COLUMN_MAP} 的集合
     */
    private final static List<Entry> ENTRY_LIST = new ArrayList<>();

    /**
     * key是表名(如果数据库名不为空，则是'数据库名.表名'), value是代理类. 主要代理被 {@link org.binlog.listener.annotation.BinLogListener} 注解的类
     */
    private final static Map<String, BinLogProxy> PROXY_MAP = new ConcurrentHashMap<>();

    /**
     * 第一个Map的key是表名，第二个Map的Key是数据库名, value是列集合
     */
    private final static Map<String, Map<String, List<Column>>> COLUMN_MAP = new ConcurrentHashMap<>();

    private BinLogListenerCore() {}

    /**
     * 根据表名和库名查询表的字段信息
     * @param tableKey 表名
     * @param dbKey 数据库名
     * @return 字段信息集合
     */
    public static List<Column> getColumn(String tableKey, String dbKey) {
        Map<String, List<Column>> innerMap = COLUMN_MAP.get(tableKey);
        return innerMap != null ? innerMap.get(dbKey) : null;
    }

    /**
     * 缓存表的字段信息
     * @param tableKey 表名
     * @param dbKey 数据库名
     * @param columnList 字段信息集合
     */
    public static void putColumn(String tableKey, String dbKey, List<Column> columnList) {
        COLUMN_MAP.computeIfAbsent(tableKey, k -> new ConcurrentHashMap<>()).put(dbKey, columnList);
    }

    /**
     * 缓存表的代理类，如果 {@link org.binlog.listener.annotation.BinLogListener} 指定了数据库名
     * 则缓存为'数据库名.表名', 否则缓存为'表名'
     * @param dbName 数据库名
     * @param tableName 表名
     * @param value 代理类
     * @throws Exception 如果表的代理类已存在则抛出异常
     */
    public static void put(String dbName, String tableName, BinLogProxy value) throws Exception {
        String key = tableName;
        if(!StringUtils.isEmpty(dbName)) {
            key = dbName + "." + tableName;
        }
        if(PROXY_MAP.containsKey(key)) {
            throw new Exception(String.format("The BinLogListener [%s] already exists.", tableName));
        }
        PROXY_MAP.put(key, value);
        ENTRY_LIST.add(new Entry(tableName, dbName));
    }

    /**
     * 检查表的代理类是否存在
     * @param key 表名(如果数据库名不为空，则是'数据库名.表名')
     * @return 是否存在
     */
    public static boolean contains(String key) {
        return COLUMN_MAP.containsKey(key);
    }
    
    public static BinLogProxy get(String key) {
        return PROXY_MAP.get(key);
    }
    
    public static List<Entry> getAllEntry() {
        return ENTRY_LIST;
    }

    /**
     * 寻找并且执行代理
     * @param tableName  表名
     * @param dbName  数据库名
     * @param data  数据信息
     */
    public static void run(String tableName, String dbName, Object data) {
        BinLogProxy binLogProxy = get(dbName + "." + tableName);
        //  如果指定数据库+表代理的不存在, 才去获取表名代理的
        if(binLogProxy == null) {
            binLogProxy = get(tableName);
        }
        if(binLogProxy == null) {
            return;
        }
        try {
            binLogProxy.intercept(null, null, new Object[]{data}, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        for (BinLogProxy proxy : PROXY_MAP.values()) {
            try {
                proxy.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class Entry {

        /**
         * 表名
         */
        private final String tableName;

        /**
         * 数据库名
         */
        private final String dbName;

        public Entry(String tableName, String dbName) {
            this.tableName = tableName;
            this.dbName = dbName;
        }

        public String getTableName() {
            return tableName;
        }

        public String getDbName() {
            return dbName;
        }
    }
    
}
