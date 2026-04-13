package org.binlog.listener.constant;

import org.binlog.listener.annotation.BinLogListener;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * @author: JiangWH
 * @date: 2024/1/26 15:49
 * @version: 1.0.0
 */
public class BinLogConstants {
    
    /**
     * 基础数据类型
     */
    public static final List<Class<?>> NORMAL_TYPE = Arrays.asList(byte.class, Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            char.class, Character.class,
            String.class,
            BigDecimal .class);

    /**
     * 操作类型
     */
    public enum OperatorType {
        /**
         * 插入
         */
        INSERT,
        /**
         * 更新
         */
        UPDATE,
        /**
         * 删除
         */
        DELETE
    }

    /**
     * MySQL binlog模式
     */
    public enum BinLogMode {
        /**
         * STATEMENT模式
         */
        STATEMENT,
        /**
         * ROW模式
         */
        ROW,
        /**
         * MIXED模式
         */
        MIXED
    }

    public enum CallbackType {
        /**
         * 默认, 保证回调 {@link org.binlog.listener.annotation.BinLogEvent} 方法前的线程安全
         */
        DEFAULT,
        /**
         * 单线程执行(等待回调方法执行完, 会阻塞其他线程)
         */
        SINGLE
    }
    
}
