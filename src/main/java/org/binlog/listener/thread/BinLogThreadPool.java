package org.binlog.listener.thread;

import org.binlog.listener.annotation.BinLogEvent;
import org.binlog.listener.constant.BinLogConstants;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: JiangWH
 * @date: 2024/1/26 14:40
 * @version: 1.0.0
 */
public class BinLogThreadPool {
    
    /**
     * 创建一个线程工厂，用于定制线程的创建
     */
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger threadId = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("BinLogPoolThread-" + threadId.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    };
    
    /**
     * 创建固定大小的线程池，使用无界队列保证任务不会被拒绝
     */
    private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(
            5,
            10,
            60L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(500),
            THREAD_FACTORY,
            new ThreadPoolExecutor.CallerRunsPolicy()
    );
    
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
                new ArrayBlockingQueue<>(200),
                THREAD_FACTORY,
                new ThreadPoolExecutor.CallerRunsPolicy()
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
