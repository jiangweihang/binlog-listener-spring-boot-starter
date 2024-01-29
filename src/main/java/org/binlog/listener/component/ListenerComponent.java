package org.binlog.listener.component;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.binlog.listener.annotation.BinLogEvent;
import org.binlog.listener.annotation.EnableBinlogListener;
import org.binlog.listener.cglib.BinLogServiceProxy;
import org.binlog.listener.core.BinLogListenerCore;
import org.binlog.listener.entity.Column;
import org.binlog.listener.property.BinLogProperty;
import org.binlog.listener.spring.SpringContextUtils;
import org.binlog.listener.tactics.BinLogListener;
import org.binlog.listener.tactics.impl.RowTypeBinLogListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * @author: JiangWH
 * @date: 2024/1/25 17:04
 * @version: 1.0.0
 */
@Component
@SuppressWarnings("all")
public class ListenerComponent implements ApplicationContextAware {
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    @Autowired
    private Environment environment;
    
    @Autowired
    private BinLogProperty binLogProperty;
    
    private static final String TABLE_SCHEMA = "TABLE_SCHEMA";
    private static final String QUERY_DB_NAME_SQL = "SELECT `" + TABLE_SCHEMA +
            "` FROM `information_schema`.`TABLES` WHERE TABLE_NAME = '%s';";
    
    private static final String COLUMN_NAME = "COLUMN_NAME";
    private static final String ORDINAL_POSITION = "ORDINAL_POSITION";
    private static final String COLUMN_KEY = "COLUMN_KEY";
    private static final String QUERY_COLUMN_LIST_SQL = "SELECT `" + COLUMN_NAME + "`, `" + ORDINAL_POSITION + "`, `" + COLUMN_KEY +
            "` FROM `information_schema`.`COLUMNS` WHERE `TABLE_NAME` = '%s' AND `TABLE_SCHEMA` = '%s';";
    
    private static final String QUERY_BINLOG_FORMAT = "SHOW VARIABLES LIKE 'binlog_format';";
    
    /**
     * 1、初始化代理信息. 获取被 {@link org.binlog.listener.annotation.BinLogListener} 修饰的类, 并且用Cglib代理 <p>
     * 2、初始化已经加载的 {@link org.binlog.listener.annotation.BinLogListener#tableName()} 表字段信息 <p>
     * 3、初始化监听器 {@link BinLogListener}
     */
    @PostConstruct
    public void init() throws Exception {
        //  1、初始化代理信息
        initCglibBinLogListener();
        
        //  2、初始化监听的表信息
        BinLogListener listener = initListenerTableInfo();
    
        //  3、初始化监听器
        initListener(listener);
    }
    
    private BinLogListener initListenerTableInfo() throws Exception {
        //  初始化一个DataSource
        DataSource dataSource = getDataSource();
        Connection connection = dataSource.getConnection();
    
        //  初始化当前代理的表信息
        Set<String> allKey = BinLogListenerCore.getAllKey();
        for (String tableName : allKey) {
            //  查询包含了这张表的数据库名称
            String queryDbNameSql = String.format(QUERY_DB_NAME_SQL, tableName);
            PreparedStatement dbStatement = connection.prepareStatement(queryDbNameSql);
            ResultSet dbRs = dbStatement.executeQuery();
            while (dbRs.next()) {
                String dbName = dbRs.getString(TABLE_SCHEMA);
                if(dbName == null || TABLE_SCHEMA.isEmpty()) {
                    continue;
                }
            
                List<Column> columns = new ArrayList<>();
                //  根据表名和库名去查询表的字段信息
                String queryColumnListSql = String.format(QUERY_COLUMN_LIST_SQL, tableName, dbName);
                PreparedStatement columnStatement = connection.prepareStatement(queryColumnListSql);
                ResultSet columnRs = columnStatement.executeQuery();
                while (columnRs.next()) {
                    String columnKey = columnRs.getString(COLUMN_KEY);
                    boolean isPrimary = columnKey != null && columnKey.equals("PRI");
                    Column item = new Column(columnRs.getString(COLUMN_NAME), columnRs.getInt(ORDINAL_POSITION), isPrimary);
                    columns.add(item);
                }
                columnRs.close();
            
                BinLogListenerCore.putColumn(tableName, dbName, columns);
            }
            dbRs.close();
        }
    
        //  查询MySQL配置binlog-format信息, 通过这个选择监听器策略
        PreparedStatement preparedStatement = connection.prepareStatement(QUERY_BINLOG_FORMAT);
        ResultSet resultSet = preparedStatement.executeQuery();
        BinLogListener listener = null;
        while (resultSet.next()) {
            String binlogFormat = resultSet.getString("Value");
            if(binlogFormat == null) {
                throw new RuntimeException("未查询到binlog配置.");
            }
            if (binlogFormat.equals("ROW")) {
                //  ROW模式
                listener = new RowTypeBinLogListener();
            } else {
                //  STATEMENT模式
            }
        }
        resultSet.close();
        connection.close();
        return listener;
    }
    
    private void initCglibBinLogListener() throws ClassNotFoundException {
        Set<String> basePackages = getBasePackages();
    
        //  创建scanner
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(resourceLoader);
    
        //  设置扫描器scanner扫描的过滤条件
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(org.binlog.listener.annotation.BinLogListener.class);
        scanner.addIncludeFilter(annotationTypeFilter);
    
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
                    Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(org.binlog.listener.annotation.BinLogListener.class.getCanonicalName());
                
                    try {
                        if(SpringContextUtils.getBean(Class.forName(annotationMetadata.getClassName())) == null) {
                            //  不存在则先注册到容器, 然后从容器中获取进行代理
                            registerSimpleRpcClient(SpringContextUtils.registry, annotationMetadata);
                        }
                        Object bean = SpringContextUtils.getBean(Class.forName(annotationMetadata.getClassName()));
                        Class<?> clazz = bean.getClass();
                        
                        for (Method method : clazz.getMethods()) {
                            //  获取下面所有被 @BinLogEvent 修饰的方法
                            BinLogEvent annotation = method.getAnnotation(BinLogEvent.class);
                            if(annotation == null) { continue; }
                        
                            //  cglib代理该类和方法
                            ClassLoader servletUtil = clazz.getClassLoader();
                            Class<?> jdkProxy = servletUtil.loadClass(candidateComponent.getBeanClassName());
                            BinLogServiceProxy binLogServiceProxy = new BinLogServiceProxy(bean, method);
                        
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
     * 利用factoryBean创建代理对象，并注册到容器
     */
    private static void registerSimpleRpcClient(BeanDefinitionRegistry registry,
                                                AnnotationMetadata annotationMetadata) throws ClassNotFoundException {
        // 类名（接口全限定名）
        String className = annotationMetadata.getClassName();
        // 创建SimpleRpcClientFactoryBean的BeanDefinition
        BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(Class.forName(className));
        
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
        
        // 注册bean定义信息到容器
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className, new String[]{className});
        // 使用BeanDefinitionReaderUtils工具类将BeanDefinition注册到容器
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }
    
    /**
     * 获取base packages <p>
     * 如果 {@link EnableBinlogListener#packages()} 有值则使用其值，如果没有则使用当前类所在的包为basePackages
     */
    private Set<String> getBasePackages() throws ClassNotFoundException {
        //  获取启动类名
        String startClassName = environment.getProperty("sun.java.command");
        Class<?> aClass = Class.forName(startClassName);
        
        //  获取配置扫描的包名
        EnableBinlogListener annotation = aClass.getAnnotation(EnableBinlogListener.class);
        String[] packages = annotation.packages();
        
        Set<String> basePackages = new HashSet<>();
        //  value 属性是否有配置值，如果有则添加
        for (String pkg : packages) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        
        //  如果上面两步都没有获取到basePackages，那么这里就默认使用当前项目启动类所在的包为basePackages
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(startClassName));
        }
        
        return basePackages;
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
     * 初始化监听器 <p>
     * 根据mysql配置的binlog-format策略选择 {@link BinLogListener}
     */
    private void initListener(BinLogListener listener) {
        new Thread(() -> {
            BinaryLogClient client = new BinaryLogClient(binLogProperty.getHost(), binLogProperty.getPort(),
                    binLogProperty.getUsername(), binLogProperty.getPassword());
            EventDeserializer eventDeserializer = new EventDeserializer();
            eventDeserializer.setCompatibilityMode(
                    EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG,
                    EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
            );
            client.setEventDeserializer(eventDeserializer);
            client.registerEventListener(listener);
            try {
                client.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        String url = String.format("jdbc:mysql://%s:%s/%s", binLogProperty.getHost(), binLogProperty.getPort(), binLogProperty.getDb());
        config.setJdbcUrl(url);
        config.setUsername(binLogProperty.getUsername());
        config.setPassword(binLogProperty.getPassword());
        
        return new HikariDataSource(config);
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtils.applicationContext = applicationContext;
    }
    
}
