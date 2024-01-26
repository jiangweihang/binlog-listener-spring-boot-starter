package org.binlog.listener.component;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.binlog.listener.core.BinLogListenerCore;
import org.binlog.listener.entity.Column;
import org.binlog.listener.property.BinLogProperty;
import org.binlog.listener.tactics.BinLogListener;
import org.binlog.listener.tactics.impl.RowTypeBinLogListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;

/**
 * @author: JiangWH
 * @date: 2024/1/25 17:04
 * @version: 1.0.0
 */
@Component
@SuppressWarnings("all")
public class ListenerComponent {

    @Autowired
    private BinLogProperty binLogProperty;
    
    private static final String TABLE_SCHEMA = "TABLE_SCHEMA";
    private static final String QUERY_DB_NAME_SQL = "SELECT `" + TABLE_SCHEMA +
            "` FROM `information_schema`.`TABLES` WHERE TABLE_NAME = '%s';";
    
    private static final String COLUMN_NAME = "COLUMN_NAME";
    private static final String ORDINAL_POSITION = "ORDINAL_POSITION";
    private static final String COLUMN_KEY = "COLUMN_KEY";
    private static final String QUERY_COLUMN_LIST_SQL = "SELECT `" + COLUMN_NAME + "`, `" + ORDINAL_POSITION + "`, `" + COLUMN_KEY +
            "` FROM `information_schema`.`COLUMNS` WHERE `TABLE_NAME` = '%s' AND `TABLE_SCHEMA` = '%s';";
    
    private static final String QUERY_BINLOG_FORMAT = "SHOW VARIABLES LIKE 'binlog_format';";
    
    /**
     * 初始化已经加载的 {@link org.binlog.listener.annotation.BinLogListener#tableName()} 表字段信息
     */
    @PostConstruct
    public void init() throws SQLException {
        //  初始化一个DataSource
        DataSource dataSource = getDataSource();
        Connection connection = dataSource.getConnection();
    
        //  初始化当前代理的表信息
        Set<String> allKey = BinLogListenerCore.getAllKey();
        for (String tableName : allKey) {
            //  查询包含了这张表的数据库名称
            String queryDbNameSql = String.format(QUERY_DB_NAME_SQL, tableName);
            PreparedStatement dbStatement = connection.prepareStatement(queryDbNameSql);
            ResultSet dbRs = dbStatement.executeQuery();
            while (dbRs.next()) {
                String dbName = dbRs.getString(TABLE_SCHEMA);
                if(dbName == null || TABLE_SCHEMA.isEmpty()) {
                    continue;
                }
    
                List<Column> columns = new ArrayList<>();
                //  根据表名和库名去查询表的字段信息
                String queryColumnListSql = String.format(QUERY_COLUMN_LIST_SQL, tableName, dbName);
                PreparedStatement columnStatement = connection.prepareStatement(queryColumnListSql);
                ResultSet columnRs = columnStatement.executeQuery();
                while (columnRs.next()) {
                    String columnKey = columnRs.getString(COLUMN_KEY);
                    boolean isPrimary = columnKey != null && columnKey.equals("PRI");
                    Column item = new Column(columnRs.getString(COLUMN_NAME), columnRs.getInt(ORDINAL_POSITION), isPrimary);
                    columns.add(item);
                }
                columnRs.close();
                
                BinLogListenerCore.putColumn(tableName, dbName, columns);
            }
            dbRs.close();
        }
        
        //  查询MySQL配置binlog-format信息, 通过这个选择监听器策略
        PreparedStatement preparedStatement = connection.prepareStatement(QUERY_BINLOG_FORMAT);
        ResultSet resultSet = preparedStatement.executeQuery();
        BinLogListener listener = null;
        while (resultSet.next()) {
            String binlogFormat = resultSet.getString("Value");
            if(binlogFormat == null) {
                throw new RuntimeException("未查询到binlog配置.");
            }
            if (binlogFormat.equals("ROW")) {
                //  ROW模式
                listener = new RowTypeBinLogListener();
            } else {
                //  STATEMENT模式
            }
        }
        resultSet.close();
        connection.close();
        initListener(listener);
    }
    
    /**
     * 初始化监听器 <p>
     * 根据mysql配置的binlog-format策略选择 {@link BinLogListener}
     */
    private void initListener(BinLogListener listener) {
        new Thread(() -> {
            BinaryLogClient client = new BinaryLogClient(binLogProperty.getHost(), binLogProperty.getPort(),
                    binLogProperty.getUsername(), binLogProperty.getPassword());
            EventDeserializer eventDeserializer = new EventDeserializer();
            eventDeserializer.setCompatibilityMode(
                    EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG,
                    EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
            );
            client.setEventDeserializer(eventDeserializer);
            client.registerEventListener(listener);
            try {
                client.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        String url = String.format("jdbc:mysql://%s:%s/%s", binLogProperty.getHost(), binLogProperty.getPort(), binLogProperty.getDb());
        config.setJdbcUrl(url);
        config.setUsername(binLogProperty.getUsername());
        config.setPassword(binLogProperty.getPassword());
        
        return new HikariDataSource(config);
    }
    
}
