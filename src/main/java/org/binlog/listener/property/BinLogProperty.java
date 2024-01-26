package org.binlog.listener.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author: JiangWH
 * @date: 2024/1/25 15:27
 * @version: 1.0.0
 */
@ConfigurationProperties(prefix = "binlog.property")
public class BinLogProperty {

    private String host;
    
    private int port;
    
    private String username;
    
    private String password;
    
    private String db;
    
    private String table;
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getDb() {
        return db;
    }
    
    public void setDb(String db) {
        this.db = db;
    }
    
    public String getTable() {
        return table;
    }
    
    public void setTable(String table) {
        this.table = table;
    }
}
