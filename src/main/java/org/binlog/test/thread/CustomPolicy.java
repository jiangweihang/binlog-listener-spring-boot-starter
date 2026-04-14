package org.binlog.test.thread;

import org.binlog.listener.thread.BinLogPolicy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: Emiya
 * @Date: 2026/4/14 16:29
 * @Description: 用户自定义拒绝策略
 */
@Component
public class CustomPolicy extends BinLogPolicy {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        System.out.println("======================啥也不做======================");
    }

}
