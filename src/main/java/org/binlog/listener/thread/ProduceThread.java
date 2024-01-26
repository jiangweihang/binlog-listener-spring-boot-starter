package org.binlog.listener.thread;

import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventHeaderV4;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author: JiangWH
 * @date: 2024/1/26 14:46
 * @version: 1.0.0
 */
public class ProduceThread implements Runnable {
    
    private final ConcurrentHashMap<String, Event> taskMap;
    
    private final Lock lock;
    
    private final Condition notEmpty;
    
    private final Event event;
    
    public ProduceThread(ConcurrentHashMap<String, Event> taskMap, Lock lock, Condition notEmpty, Event event) {
        this.taskMap = taskMap;
        this.lock = lock;
        this.notEmpty = notEmpty;
        this.event = event;
    }
    
    @Override
    public void run() {
        lock.lock();
        try {
            EventHeaderV4 header = event.getHeader();
            String key = header.getNextPosition() + "";
            while (taskMap.containsKey(key)) {
                //  如果key已存在，则等待
                notEmpty.await();
            }
            taskMap.put(key, event);
            //  生产后唤醒所有等待的消费者
            notEmpty.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
    
}
