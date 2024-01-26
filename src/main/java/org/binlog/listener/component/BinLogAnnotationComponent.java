package org.binlog.listener.component;

import org.binlog.listener.annotation.BinLogEvent;
import org.binlog.listener.annotation.BinLogListener;
import org.binlog.listener.annotation.EnableBinlogListener;
import org.binlog.listener.cglib.BinLogServiceProxy;
import org.binlog.listener.core.BinLogListenerCore;
import org.binlog.listener.property.BinLogProperty;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 扫描被 {@link BinLogListener} 注解的类
 * @author: JiangWH
 * @date: 2024/1/25 16:00
 * @version: 1.0.0
 */
@Component
@ConditionalOnBean(BinLogProperty.class)
public class BinLogAnnotationComponent implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
    
    /**
     * 资源加载器
     */
    private ResourceLoader resourceLoader;
    
    /**
     * 环境
     */
    private Environment environment;
    
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        //  创建scanner
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(resourceLoader);
        
        //  设置扫描器scanner扫描的过滤条件
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(BinLogListener.class);
        scanner.addIncludeFilter(annotationTypeFilter);
        
        //  获取指定要扫描的basePackages
        Set<String> basePackages = getBasePackages(metadata);
        
        //  遍历每一个basePackages
        for (String basePackage : basePackages) {
            //  通过scanner获取basePackage下的候选类(有标@SimpleRpcClient注解的类)
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
            //  遍历每一个候选类，如果符合条件就把他们注册到容器
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                    //  获取注解的属性
                    Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(BinLogListener.class.getCanonicalName());
                    
                    //  获取下面所有被 @BinLogEvent 修饰的方法
                    try {
                        Class<?> clazz = Class.forName(candidateComponent.getBeanClassName());
                        Method[] methods = clazz.getMethods();
                        for (Method method : methods) {
                            BinLogEvent annotation = method.getAnnotation(BinLogEvent.class);
                            if(annotation == null) { continue; }
                            
                            //  cglib代理该类和方法
                            ClassLoader servletUtil = clazz.getClassLoader();
                            Class<?> jdkProxy = servletUtil.loadClass(candidateComponent.getBeanClassName());
                            Object object = jdkProxy.newInstance();
                            BinLogServiceProxy binLogServiceProxy = new BinLogServiceProxy(object, method);
                            
                            //  根据表名放到内存中管理
                            String tableName = attributes.get("tableName").toString();
                            if(BinLogListenerCore.contains(tableName)) {
                                throw new Exception(String.format("The tableName [%s] already exists.", tableName));
                            }
                            BinLogListenerCore.put(tableName, binLogServiceProxy);
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    /**
     * 创建扫描器
     */
    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent()) {
                    if (!beanDefinition.getMetadata().isAnnotation()) {
                        isCandidate = true;
                    }
                }
                return isCandidate;
            }
        };
    }
    
    /**
     * 获取base packages <p>
     * 如果 {@link EnableBinlogListener#packages()} 有值则使用其值，如果没有则使用当前类所在的包为basePackages
     */
    protected static Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        //  获取到@EnableSimpleRpcClients注解所有属性
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableBinlogListener.class.getCanonicalName());
        Set<String> basePackages = new HashSet<>();
        assert attributes != null;
        //  value 属性是否有配置值，如果有则添加
        for (String pkg : (String[]) attributes.get("packages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }

        //  如果上面两步都没有获取到basePackages，那么这里就默认使用当前项目启动类所在的包为basePackages
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }

        return basePackages;
    }
    
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
    
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
