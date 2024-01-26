package org.binlog.listener.annotation;

import java.lang.annotation.*;

/**
 * 标记监听到 {@link #tableName()} 的回调类
 * @author Emiya
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface BinLogListener {
    
    /**
     * 表名
     */
    String tableName();

}
