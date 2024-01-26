通过监听MySQL的binlog，将binlog解析成指定实体类形式, 调用被 @BinLogListener 注解标注的方法

目前只支持MySQL binlog-format=ROW 的模式

使用例子
```java
@EnableBinlogListener
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class BinLogApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BinLogApplication.class, args);
    }
    
}
```

```java
@BinLogListener(tableName = "t_a_temp")
public class TestService {
    
    @BinLogEvent
    public void event(BinLogDataDto dto) {
        System.out.println(JSONObject.toJSONString(dto));
    }
    
}
```

```yaml
spring:
  application:
    name: binlog-listener-spring-boot-starter

server:
  port: 8886

binlog:
  property:
    host: localhost
    port: 13306
    username: root
    password: mysql
    db: labscare_base_huaxi
    table: t_a_temp
```

输出结果:
新增:
```json
{
  "columns": [
    {
      "name": "id",
      "position": 1,
      "primary": true
    }
    ...
  ],
  "data": [
    {
      "creator": "4",
      "companyId": "4",
      "gaugingId": "4",
      "sampleId": "4",
      "createTime": "4",
      "id": "6",
      "selecter": "4",
      "userId": "6"
    },
    {
      "creator": "4",
      "companyId": "4",
      "gaugingId": "4",
      "sampleId": "4",
      "createTime": "4",
      "id": "5",
      "selecter": "4",
      "userId": "6"
    }
  ],
  "dbName": "labscare_base",
  "tableName": "t_a_temp",
  "type": "INSERT",
  "updateBefore": []
}
```
更新:
```json
{
  "columns": [
    {
      "name": "id",
      "position": 1,
      "primary": true
    }
    ...
  ],
  "data": [
    {
      "creator": "4",
      "companyId": "4",
      "gaugingId": "4",
      "sampleId": "4",
      "createTime": "4",
      "id": "5",
      "selecter": "4",
      "userId": "6"
    },
    {
      "creator": "4",
      "companyId": "4",
      "gaugingId": "4",
      "sampleId": "4",
      "createTime": "4",
      "id": "6",
      "selecter": "4",
      "userId": "6"
    }
  ],
  "dbName": "labscare_base",
  "tableName": "t_a_temp",
  "type": "UPDATE",
  "updateBefore": [
    {
      "creator": "4",
      "companyId": "4",
      "gaugingId": "1",
      "sampleId": "4",
      "createTime": "4",
      "id": "5",
      "selecter": "4",
      "userId": "6"
    },
    {
      "creator": "4",
      "companyId": "4",
      "gaugingId": "1",
      "sampleId": "4",
      "createTime": "4",
      "id": "6",
      "selecter": "4",
      "userId": "6"
    }
  ]
}
```
删除:
```json
{
  "columns": [
    {
      "name": "id",
      "position": 1,
      "primary": true
    },
    {
      "name": "gaugingId",
      "position": 2,
      "primary": false
    }
    ...
  ],
  "data": [
    {
      "creator": "4",
      "companyId": "4",
      "gaugingId": "4",
      "sampleId": "4",
      "createTime": "4",
      "id": "5",
      "selecter": "4",
      "userId": "6"
    }
  ],
  "dbName": "labscare_base",
  "tableName": "t_a_temp",
  "type": "DELETE",
  "updateBefore": []
}
```
