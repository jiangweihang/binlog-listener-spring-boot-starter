package org.binlog.listener.constant;

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
    
    public static enum OperatorType {
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
    
}
