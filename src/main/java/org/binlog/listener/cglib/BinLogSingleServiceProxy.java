package org.binlog.listener.cglib;

import net.sf.cglib.proxy.MethodProxy;
import org.binlog.listener.thread.BinLogThreadPool;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Emiya
 * @Date: 2026/4/5 00:41
 * @Description: 单线程执行模式
 */
public class BinLogSingleServiceProxy extends BinLogProxy {

    private final ExecutorService executor;

    public BinLogSingleServiceProxy(Object object, Method method) {
        super(object, method);
        this.executor = BinLogThreadPool.createPool();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Object[] args = super.intercept(objects);
        executor.execute(() -> {
            try {
                METHOD.invoke(OBJECT, args);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        return null;
    }

    @Override
    public void destroy() {
        BinLogThreadPool.shutdown(executor, 10, TimeUnit.SECONDS);
    }

}
