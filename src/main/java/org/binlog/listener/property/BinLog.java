package org.binlog.listener.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author: Emiya
 * @Date: 2026/4/14 15:37
 * @Description:
 */
@ConfigurationProperties(prefix = "binlog")
public class BinLog {

    private BinLogProperty property;

    private BinLogThreadProperty thread;

    public BinLogProperty getProperty() {
        return property;
    }

    public void setProperty(BinLogProperty property) {
        this.property = property;
    }

    public BinLogThreadProperty getThread() {
        return thread;
    }

    public void setThread(BinLogThreadProperty thread) {
        this.thread = thread;
    }

}
