# ddf-common-mongo

> MongoDB 集成辅助模块。提供 `MongoTemplate` 操作封装和分页工具，作为 MongoDB 相关能力的共享组织入口。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-mongo` 解决的是 **"MongoDB 常用操作的快速接入"** 问题。

| 场景 | 典型问题 | 模块提供的能力 |
| --- | --- | --- |
| 文档型数据存储 | 需要灵活的 Schema，关系型数据库不合适 | `MongoTemplate` 封装与分页辅助 |
| 日志/事件存储 | 海量半结构化数据写入 | 基于 MongoDB 的高吞吐写入 |
| 已有 MongoDB 集群 | 业务需要快速接入 | 自动配置 + 工具类，减少样板代码 |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-mongo</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. 最小化配置

```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: your_database
      username: admin
      password: password
      authentication-database: admin
```

---

## 4. 核心 API

### 4.1 MongoTemplate 基础操作

模块已引入 `spring-boot-starter-data-mongodb`，`MongoTemplate` Bean 自动可用：

```java
@Autowired
private MongoTemplate mongoTemplate;

// 插入文档
User user = new User("name", 25);
mongoTemplate.insert(user);

// 查询文档
Query query = new Query(Criteria.where("name").is("name"));
User found = mongoTemplate.findOne(query, User.class);

// 更新文档
Update update = new Update().set("age", 26);
UpdateResult result = mongoTemplate.updateFirst(query, update, User.class);

// 删除文档
DeleteResult result = mongoTemplate.remove(query, User.class);
```

### 4.2 分页查询

```java
@Autowired
private MongoTemplateHelper mongoHelper;

PageRequest pageRequest = PageRequest.of(0, 10);
Query query = new Query(Criteria.where("status").is(1));

Page<User> page = mongoHelper.handlerPage(query, User.class, pageRequest);
List<User> list = page.getContent();
long total = page.getTotalElements();
```

---

## 5. 进阶用法 / 扩展点

### 5.1 集合名自定义

默认使用类名作为集合名，可通过 `@Document` 注解指定：

```java
@Document(collection = "custom_users")
public class User {
    @Id
    private String id;
    private String name;
}
```

### 5.2 索引管理

建议在应用启动时创建必要索引：

```java
@PostConstruct
public void initIndexes() {
    mongoTemplate.indexOps(User.class).ensureIndex(
        new Index().on("name", Sort.Direction.ASC).unique()
    );
}
```

### 5.3 复杂聚合查询

直接使用 `MongoTemplate` 的 `aggregate` 方法：

```java
Aggregation aggregation = Aggregation.newAggregation(
    Aggregation.match(Criteria.where("status").is(1)),
    Aggregation.group("category").sum("amount").as("total"),
    Aggregation.sort(Sort.Direction.DESC, "total")
);
AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "orders", Document.class);
```

---

## 6. 与其他模块协作

| 模块 | 协作方式 |
| --- | --- |
| `ddf-common-core` | JSON 工具、日期处理等基础能力 |
| `ddf-common-starter-web` | Web 接口层接收请求后写入 MongoDB |

---

## 7. FAQ

**Q1：本模块是否包含完整的 Repository 封装？**
当前主要是 `MongoTemplate` 操作辅助和分页工具。更完整的 Repository、查询模型、分页能力建议在业务代码或后续专用模块中构建。

**Q2：`MongoTemplate` Bean 由谁提供？**
由 `spring-boot-starter-data-mongodb` 自动配置提供，本模块仅做操作封装，不替代 Spring Data MongoDB。

**Q3：是否支持 MongoDB 事务？**
支持。Spring Data MongoDB 提供 `@Transactional` 支持，但要求 MongoDB 副本集部署。

---

## 8. 参考

- 源码：`MongoAutoConfiguration`、`MongoTemplateHelper`
- Spring Data MongoDB：https://docs.spring.io/spring-data/mongodb/reference/
