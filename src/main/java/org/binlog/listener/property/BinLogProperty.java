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

}
