package org.binlog.listener.thread;

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
            thread.setDaemon(true); // 设置为守护线程（取决于你的需求）
            return thread;
        }
    };
    
    /**
     * 创建固定大小的线程池，使用无界队列保证任务不会被拒绝
     */
    private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(
            // 核心线程数
            5,
            // 最大线程数，这里设置与核心线程数相同，意味着线程池大小是固定的
            10,
            // 空闲线程存活时间（单位：秒），由于线程池大小固定且队列无界，此参数在此场景下实际意义不大
            60L,
            TimeUnit.SECONDS,
            // 工作队列，使用无界队列LinkedBlockingQueue，确保任务不会被拒绝
            new LinkedBlockingQueue<Runnable>(500),
            // 线程工厂，用于创建新线程，这里使用默认的线程工厂
            THREAD_FACTORY
    );
    
    public static void executeTask(Runnable task) {
        THREAD_POOL.execute(task);
    }

    public static ThreadPoolExecutor createPool() {
        return new ThreadPoolExecutor(
                1,
                1,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(200),
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
