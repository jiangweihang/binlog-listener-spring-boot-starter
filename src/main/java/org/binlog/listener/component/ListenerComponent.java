package org.binlog.listener.component;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import org.binlog.listener.annotation.BinLogEvent;
import org.binlog.listener.annotation.EnableBinlogListener;
import org.binlog.listener.cglib.BinLogProxy;
import org.binlog.listener.cglib.BinLogServiceProxy;
import org.binlog.listener.cglib.BinLogSingleServiceProxy;
import org.binlog.listener.constant.BinLogConstants;
import org.binlog.listener.core.BinLogListenerCore;
import org.binlog.listener.entity.Column;
import org.binlog.listener.property.BinLog;
import org.binlog.listener.property.BinLogProperty;
import org.binlog.listener.spring.SpringContextUtils;
import org.binlog.listener.tactics.BinLogListener;
import org.binlog.listener.tactics.impl.MixedTypeBinLogListener;
import org.binlog.listener.tactics.impl.RowTypeBinLogListener;
import org.binlog.listener.tactics.impl.StatementTypeBinLogListener;
import org.binlog.listener.thread.BinLogPolicy;
import org.binlog.listener.thread.BinLogThreadPool;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

    private final ResourceLoader resourceLoader;

    private final Environment environment;

    private final BinLog binLog;

    private final JdbcTemplate jdbcTemplate;

    private final BinLogPolicy binLogPolicy;

    public ListenerComponent(ResourceLoader resourceLoader, Environment environment,
                             BinLog binLog, JdbcTemplate jdbcTemplate, @Autowired(required = false) BinLogPolicy binLogPolicy) {
        this.resourceLoader = resourceLoader;
        this.environment = environment;
        this.binLog = binLog;
        this.jdbcTemplate = jdbcTemplate;
        this.binLogPolicy = binLogPolicy;
    }

    /**
     * 数据库名(字段名称)
     */
    private static final String TABLE_SCHEMA = "TABLE_SCHEMA";

    /**
     * 查询数据库名的SQL语句
     */
    private static final String QUERY_DB_NAME_SQL = "SELECT `" + TABLE_SCHEMA +
            "` FROM `information_schema`.`TABLES` WHERE TABLE_NAME = '%s';";

    /**
     * 表名(字段名称)
     */
    private static final String COLUMN_NAME = "COLUMN_NAME";

    /**
    * 字段序号(字段名称)
     */
    private static final String ORDINAL_POSITION = "ORDINAL_POSITION";

    /**
     * 是否为主键(字段名称)
     */
    private static final String COLUMN_KEY = "COLUMN_KEY";

    /**
     * 查询表字段的SQL语句
     */
    private static final String QUERY_COLUMN_LIST_SQL = "SELECT `" + COLUMN_NAME + "`, `" + ORDINAL_POSITION + "`, `" + COLUMN_KEY +
            "` FROM `information_schema`.`COLUMNS` WHERE `TABLE_NAME` = '%s' AND `TABLE_SCHEMA` = '%s';";

    /**
     * 查询binlog-format的SQL语句
     */
    private static final String QUERY_BINLOG_FORMAT = "SHOW VARIABLES LIKE 'binlog_format';";

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtils.applicationContext = applicationContext;
    }

    /**
     * 1、初始化代理信息. 获取被 {@link org.binlog.listener.annotation.BinLogListener} 修饰的类, 并且用Cglib代理 <p>
     * 2、初始化已经加载的 {@link org.binlog.listener.annotation.BinLogListener#tableName()} 表字段信息 <p>
     * 3、初始化监听器 {@link org.binlog.listener.annotation.BinLogListener}
     */
    @PostConstruct
    public void init() throws Exception {
        //  1、初始化线程池信息
        new BinLogThreadPool(binLog, binLogPolicy);

        //  2、初始化代理信息
        initCglibBinLogListener();

        //  3、初始化监听的表信息
        BinLogListener listener = initListenerTableInfo();

        //  4、初始化监听器
        initListener(listener);
    }

    /**
     * 初始化代理信息
     */
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
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
            //  遍历每一个候选类，如果符合条件就把他们注册到容器
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                    //  获取注解的属性
                    Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(org.binlog.listener.annotation.BinLogListener.class.getCanonicalName());

                    try {
                        if (SpringContextUtils.getBean(Class.forName(annotationMetadata.getClassName())) == null) {
                            //  不存在则先注册到容器, 然后从容器中获取进行代理
                            registerSimpleRpcClient(SpringContextUtils.registry, annotationMetadata);
                        }
                        Object bean = SpringContextUtils.getBean(Class.forName(annotationMetadata.getClassName()));
                        Class<?> clazz = bean.getClass();

                        createBinLogProxy(clazz, candidateComponent, bean, attributes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 创建binlog代理对象
     * 搜索被 {@link org.binlog.listener.annotation.BinLogEvent} 修饰的方法, 并且用Cglib代理该类和方法(只代理一个方法)
     * 私有方法会抛出异常
     */
    private void createBinLogProxy(Class<?> clazz, BeanDefinition candidateComponent, Object bean, Map<String, Object> attributes) throws Exception {
        for (Method method : clazz.getDeclaredMethods()) {
            //  获取下面所有被 @BinLogEvent 修饰的方法
            BinLogEvent annotation = method.getAnnotation(BinLogEvent.class);
            if (annotation == null) {
                continue;
            }

            //  私有方法无法代理
            if (Modifier.isPrivate(method.getModifiers())) {
                throw new Exception(String.format("The method [%s] is private, cannot be proxied.", clazz.getName() + "." + method.getName()));
            }

            //  cglib代理该类和方法
            ClassLoader servletUtil = clazz.getClassLoader();
            Class<?> jdkProxy = servletUtil.loadClass(candidateComponent.getBeanClassName());

            BinLogProxy binLogServiceProxy = createBinLogProxy(annotation, bean, method);

            //  根据表名和数据库名称放到内存中管理
            String dbName = attributes.get("dbName").toString();
            String tableName = attributes.get("tableName").toString();
            BinLogListenerCore.put(dbName, tableName, binLogServiceProxy);
            break;
        }
    }

    /**
     * 根据配置的 {@link org.binlog.listener.annotation.BinLogEvent#callbackType()} 选择不同的代理类
     */
    private BinLogProxy createBinLogProxy(BinLogEvent annotation, Object bean, Method method) {
        if (annotation.callbackType() == BinLogConstants.CallbackType.SINGLE) {
            return new BinLogSingleServiceProxy(bean, method);
        } else {
            return new BinLogServiceProxy(bean, method);
        }
    }

    private BinLogListener initListenerTableInfo() throws Exception {
        //  初始化一个DataSource
        DataSource dataSource = jdbcTemplate.getDataSource();
        Connection connection = dataSource.getConnection();

        //  初始化当前代理的表信息
        List<BinLogListenerCore.Entry> allKey = BinLogListenerCore.getAllEntry();
        for (BinLogListenerCore.Entry entry : allKey) {
            String tableName = entry.getTableName();
            String dbName = entry.getDbName();

            //  如果数据库名不为空则直接根据库名+表名查询列信息
            if (!StringUtils.isEmpty(dbName)) {
                columnInfo(tableName, dbName, connection);
            } else {

                //  否则去查询包含这张表的所有数据库名称
                String queryDbNameSql = String.format(QUERY_DB_NAME_SQL, tableName);
                PreparedStatement dbStatement = connection.prepareStatement(queryDbNameSql);
                ResultSet dbRs = dbStatement.executeQuery();
                while (dbRs.next()) {
                    dbName = dbRs.getString(TABLE_SCHEMA);
                    columnInfo(tableName, dbName, connection);
                }
                dbRs.close();
            }
        }

        //  查询MySQL配置binlog-format信息, 通过这个选择监听器策略
        PreparedStatement preparedStatement = connection.prepareStatement(QUERY_BINLOG_FORMAT);
        ResultSet resultSet = preparedStatement.executeQuery();
        BinLogListener listener = null;
        while (resultSet.next()) {
            String binlogFormat = resultSet.getString("Value");
            if (binlogFormat == null) {
                throw new RuntimeException("The binlog configuration is not enable.");
            }
            if (binlogFormat.equalsIgnoreCase(BinLogConstants.BinLogMode.ROW.name())) {
                //  ROW模式
                listener = new RowTypeBinLogListener();
            } else if (binlogFormat.equalsIgnoreCase(BinLogConstants.BinLogMode.STATEMENT.name())) {
                //  STATEMENT模式
                listener = new StatementTypeBinLogListener();
            } else if (binlogFormat.equalsIgnoreCase(BinLogConstants.BinLogMode.MIXED.name())) {
                //  MIXED模式
                listener = new MixedTypeBinLogListener();
            }
        }
        resultSet.close();
        connection.close();
        return listener;
    }

    /**
     * 根据表名和库名查询表的字段信息并缓存
     * @param tableName 表名
     * @param dbName 数据库名
     * @param connection 数据库连接
     * @throws Exception 异常信息
     */
    private void columnInfo(String tableName, String dbName, Connection connection) throws Exception {
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

    /**
     * 利用factoryBean创建代理对象，并注册到容器
     */
    private void registerSimpleRpcClient(BeanDefinitionRegistry registry,
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
     * 如果 {@link org.binlog.listener.annotation.EnableBinlogListener#packages()} 有值则使用其值，如果没有则使用当前类所在的包为basePackages
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
    private ClassPathScanningCandidateComponentProvider getScanner() {
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
     * 根据mysql配置的binlog-format策略选择 {@link org.binlog.listener.annotation.BinLogListener}
     */
    private void initListener(BinLogListener listener) {
        BinLogProperty property = binLog.getProperty();
        new Thread(() -> {
            BinaryLogClient client = new BinaryLogClient(property.getHost(), property.getPort(),
                    property.getUsername(), property.getPassword());
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

    @PreDestroy
    public void destroy() {
        BinLogThreadPool.shutdown();
        BinLogListenerCore.shutdown();
    }

}
