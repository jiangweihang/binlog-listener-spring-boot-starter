package org.binlog.listener.annotation;

import java.lang.annotation.*;

/**
 * 标记回调方法, 一个类只有只作用一个方法
 * @author Emiya
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface BinLogEvent {
}
