package org.binlog.listener.cglib;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @Author: Emiya
 * @Date: 2026/4/5 00:48
 * @Description: 处理一些通用操作
 */
public abstract class BinLogProxy  implements MethodInterceptor {

    protected final Object OBJECT;

    protected final Method METHOD;

    public BinLogProxy(Object object, Method method) {
        this.OBJECT = object;
        this.METHOD = method;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        return METHOD.invoke(OBJECT, objects);
    }

    protected Object[] intercept(Object[] objects) throws Throwable {
        Class<?>[] parameterTypes = METHOD.getParameterTypes();
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
        return args;
    }

    public void destroy() {

    }

}