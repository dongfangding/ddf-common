# ddf-common-mongo

MongoDB 集成模块，提供 MongoDB 操作封装。

## 功能特性

- MongoDB 客户端集成
- 分页查询支持
- 常用操作封装

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-mongo</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心类

| 类路径 | 功能 |
|-------|------|
| `MongoTemplate` | MongoDB 模板 |
| `MongoProperties` | 配置属性 |
| `PageUtil` | 分页工具 |

## 使用说明

```java
@Autowired
private MongoTemplate mongoTemplate;

// 保存文档
mongoTemplate.save(entity);

// 查询
List<Entity> list = mongoTemplate.find(query, Entity.class);
```
