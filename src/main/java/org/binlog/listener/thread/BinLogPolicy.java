package org.binlog.listener.thread;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: Emiya
 * @Date: 2026/4/14 16:06
 * @Description: BinLog线程池默认拒绝策略
 */
public class BinLogPolicy implements RejectedExecutionHandler {

    /**
     * 默认使用 {@link ThreadPoolExecutor.CallerRunsPolicy} 策略
     * @param r the runnable task requested to be executed
     * @param executor the executor attempting to execute this task
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (!executor.isShutdown()) {
            r.run();
        }
    }

}
