package org.binlog.listener.annotation;

import org.binlog.listener.component.BinLogAnnotationComponent;
import org.binlog.listener.component.ListenerComponent;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 是否开启 binlog 的监听
 * @author Emiya
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({BinLogAnnotationComponent.class, ListenerComponent.class})
public @interface EnableBinlogListener {
    
    /**
     * 是否开启
     */
    boolean value() default true;
    
    /**
     * 扫描的包名, 默认为当前启动类下的所有包
     */
    String[] packages() default {};
    
}
