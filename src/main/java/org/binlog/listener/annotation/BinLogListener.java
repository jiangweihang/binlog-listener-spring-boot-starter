package org.binlog.listener.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * 标记监听到 {@link #tableName()}表 的回调类
 * @author Emiya
 */
@Service
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface BinLogListener {
    
    /**
     * 表名
     */
    String tableName();

}
