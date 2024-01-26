package org.binlog.listener.entity;

/**
 * @author: JiangWH
 * @date: 2024/1/26 9:34
 * @version: 1.0.0
 */
public class Column {

    private String name;
    
    private Integer position;
    
    private Boolean isPrimary;
    
    public Column() {}
    
    public Column(String name, Integer position, Boolean isPrimary) {
        this.name = name;
        this.position = position;
        this.isPrimary = isPrimary;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getPosition() {
        return position;
    }
    
    public void setPosition(Integer position) {
        this.position = position;
    }
    
    public Boolean getPrimary() {
        return isPrimary;
    }
    
    public void setPrimary(Boolean primary) {
        isPrimary = primary;
    }
}
