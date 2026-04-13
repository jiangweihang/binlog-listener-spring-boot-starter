package org.binlog.listener.cglib;

import net.sf.cglib.proxy.MethodProxy;
import org.binlog.listener.thread.BinLogThreadPool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 代理被 {@link org.binlog.listener.annotation.BinLogListener} 标记的类
 * 使用默认模式执行，只保证回调前的线程安全，回调后线程安全需要自己处理
 * @author: JiangWH
 * @date: 2024/1/25 16:38
 * @version: 1.0.0
 */
public class BinLogServiceProxy extends BinLogProxy {

    public BinLogServiceProxy(Object object, Method method) {
        super(object, method);
    }
    
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Object [] args = super.intercept(objects);
        BinLogThreadPool.executeTask(new Thread(() -> {
            try {
                METHOD.invoke(OBJECT, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }));
        return null;
    }
    
}
