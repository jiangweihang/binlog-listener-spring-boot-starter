package org.binlog.listener.property;

/**
 * @Author: Emiya
 * @Date: 2026/4/14 15:39
 * @Description:
 */
public class BinLogThreadProperty {

    /**
     * 核心线程数
     */
    private int corePoolSize = 5;

    /**
     * 最大线程数
     */
    private int maximumPoolSize = 5;

    /**
     * 空闲线程超时时间(秒)
     */
    private long keepAliveTimeSeconds = 60L;

    /**
     * 任务列容量
     */
    private int queueCapacity = 500;

    /**
     * 线程名称前缀
     */
    private String threadName = "BinLogPoolThread-";

    /**
     * 是否为守护线程
     */
    private boolean daemon = true;

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public long getKeepAliveTimeSeconds() {
        return keepAliveTimeSeconds;
    }

    public void setKeepAliveTimeSeconds(long keepAliveTimeSeconds) {
        this.keepAliveTimeSeconds = keepAliveTimeSeconds;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

}
