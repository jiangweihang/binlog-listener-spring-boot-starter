package org.binlog.listener.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;

/**
 * @author: JiangWH
 * @date: 2024/1/29 10:57
 * @version: 1.0.0
 */
public class SpringContextUtils {
    
    public static BeanDefinitionRegistry registry;
    
    public static ApplicationContext applicationContext;
    
    public static <T> T getBean(Class<T> clazz) {
        try {
            return applicationContext.getBean(clazz);
        } catch (Exception e) {
            return null;
        }
    }
    
}
