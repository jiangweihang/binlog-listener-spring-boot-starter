package org.binlog.listener.thread;

import org.binlog.listener.annotation.BinLogEvent;
import org.binlog.listener.constant.BinLogConstants;
import org.binlog.listener.property.BinLog;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: JiangWH
 * @date: 2024/1/26 14:40
 * @version: 1.0.0
 */
public class BinLogThreadPool {

    private final BinLog binLog;

    /**
     * 创建一个线程工厂，用于定制线程的创建
     */
    private static ThreadFactory THREAD_FACTORY = null;

    /**
     * 创建固定大小的线程池，使用无界队列保证任务不会被拒绝
     */
    private static ExecutorService THREAD_POOL = null;

    private static RejectedExecutionHandler handler = null;

    public BinLogThreadPool(BinLog binLog, RejectedExecutionHandler rejectedExecutionHandler) {
        this.binLog = binLog;

        //  初始化线程池拒绝策略
        handler = rejectedExecutionHandler != null ?
                rejectedExecutionHandler : new BinLogPolicy();

        //  初始化线程工厂
        THREAD_FACTORY = new ThreadFactory() {
            private final AtomicInteger threadId = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(binLog.getThread().getThreadName() + threadId.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        };

        //  初始化线程池
        THREAD_POOL = new ThreadPoolExecutor(
                binLog.getThread().getCorePoolSize(),
                binLog.getThread().getMaximumPoolSize(),
                binLog.getThread().getKeepAliveTimeSeconds(),
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(binLog.getThread().getQueueCapacity()),
                THREAD_FACTORY,
                handler
        );
    }

    public static void executeTask(Runnable task) {
        THREAD_POOL.execute(task);
    }

    /**
     * 创建一个单线程的线程池，用于处理阻塞的监听执行 {@link BinLogEvent#callbackType()} = {@link BinLogConstants.CallbackType#SINGLE}
     * @return
     */
    public static ThreadPoolExecutor createPool() {
        return new ThreadPoolExecutor(
                1,
                1,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                THREAD_FACTORY,
                handler
        );
    }

    public static void shutdown() {
        shutdown(THREAD_POOL, 10, TimeUnit.SECONDS);
    }

    public static void shutdown(ExecutorService executor, long timeout, TimeUnit unit) {
        if (executor.isTerminated()) {
            return;
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout, unit)) {
                System.err.println("线程池未在 " + timeout + " " + unit + " 内终止，尝试强制关闭...");
                List<Runnable> droppedTasks = executor.shutdownNow();
                System.out.println("被丢弃（未执行）的任务数量: " + droppedTasks.size());
                if (!executor.awaitTermination(timeout, unit)) {
                    System.err.println("线程池仍未能终止，可能存在无法中断的阻塞任务！");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
}
