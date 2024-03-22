package org.binlog.listener.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * 代理被 {@link org.binlog.listener.annotation.BinLogListener} 标记的类
 * @author: JiangWH
 * @date: 2024/1/25 16:38
 * @version: 1.0.0
 */
public class BinLogServiceProxy implements MethodInterceptor {
    
    private final Object object;
    
    private final Method method;
    
    public BinLogServiceProxy(Object object, Method method) {
        this.object = object;
        this.method = method;
    }
    
    public Object getProxyInstance() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(object.getClass());
        enhancer.setCallback(this);
        return enhancer.create();
    }
    
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        return this.intercept(objects, methodProxy);
    }
    
    private Object intercept(Object[] objects, MethodProxy methodProxy) throws Throwable {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        if(args.length == 0) {
            //  方法没有参数就传空
            args = null;
        } else {
            //  适配方法的参数数量, 多余的设置为空
            for(int i = 0; i < args.length; i++) {
                if(objects.length > i) {
                    args[i] = objects[i];
                }
            }
        }
        return method.invoke(object, args);
    }
    
}
