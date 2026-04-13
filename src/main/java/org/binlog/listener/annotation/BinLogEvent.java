package org.binlog.listener.annotation;

import org.binlog.listener.constant.BinLogConstants;

import java.lang.annotation.*;

/**
 * 标记回调方法, 一个类只作用一个方法
 * @author Emiya
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface BinLogEvent {

    /**
     * 回调方式
     */
    BinLogConstants.CallbackType callbackType() default BinLogConstants.CallbackType.DEFAULT;

}
