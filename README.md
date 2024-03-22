# 通过监听MySQL的binlog，将binlog解析成指定实体类形式, 调用被 @BinLogListener 注解标注的方法

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
## 以下是ROW模式的返回值
### 新增:
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
    ...
  ],
  "dbName": "labscare_base",
  "tableName": "t_a_temp",
  "type": "INSERT",
  "updateBefore": []
}
```
### 更新:
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
    ...
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
    ...
  ]
}
```
### 删除:
```json
{
  "columns": [
    {
      "name": "id",
      "position": 1,
      "primary": true
    },
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
    ...
  ],
  "dbName": "labscare_base",
  "tableName": "t_a_temp",
  "type": "DELETE",
  "updateBefore": []
}
```


## 以下是STATEMENT模式的返回值
### 新增:
```json
{"columns":["`id`","`gaugingId`","`sampleId`","`userId`","`createTime`","`creator`","`companyId`","`selecter`"],"dbName":"labscare_base_huaxi","sql":"INSERT INTO `labscare_base_huaxi`.`t_a_temp`(`id`, `gaugingId`, `sampleId`, `userId`, `createTime`, `creator`, `companyId`, `selecter`) VALUES (7, 4, 4, 6, '4', '4', 4, 4)","tableName":"t_a_temp","type":"INSERT","valuesClause":[["7","4","4","6","'4'","'4'","4","4"]]}
```
### 更新:
```json
{"dbName":"labscare_base_huaxi","setValues":{"`userId`":"6","`companyId`":"4","`createTime`":"'4'","`selecter`":"4","`sampleId`":"2","`gaugingId`":"1","`creator`":"'4'"},"sql":"UPDATE `t_a_temp` SET `gaugingId` = 1,  `sampleId` = 2, `userId` = 6,\r\n`createTime` = '4', `creator` = '4', `companyId` = 4, `selecter` = 4\r\nWHERE `id` = 6 and selecter = 4","tableName":"t_a_temp","type":"UPDATE","wheres":[{"column":"`id`","operator":"Equality","value":"6"},{"column":"selecter","operator":"Equality","value":"4"}]}
```
### 删除:
```json
{"dbName":"labscare_base_huaxi","sql":"DELETE FROM t_a_temp WHERE id = 7 AND userId = 6 AND creator = 4","tableName":"t_a_temp","type":"DELETE","wheres":[{"column":"id","operator":"Equality","value":"7"},{"column":"userId","operator":"Equality","value":"6"},{"column":"creator","operator":"Equality","value":"4"}]}
```

## @BinLogListener
有两个参数, 一个tableName, 一个dbName.  
在tableName相同的情况下, 有dbName的优先级大于没有dbName的.
