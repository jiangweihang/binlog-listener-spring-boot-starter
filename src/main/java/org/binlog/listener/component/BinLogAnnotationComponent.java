package org.binlog.listener.component;

import org.binlog.listener.spring.SpringContextUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * @author: JiangWH
 * @date: 2024/1/25 16:00
 * @version: 1.0.0
 */
@Component
public class BinLogAnnotationComponent implements ImportBeanDefinitionRegistrar {
    
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        SpringContextUtils.registry = registry;
    }

}
