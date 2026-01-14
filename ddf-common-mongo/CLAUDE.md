# CLAUDE.md

## 模块简介

提供 MongoDB 集成支持，包含常用操作封装和分页工具类。

## 核心类

| 类路径                                                | 功能           |
|----------------------------------------------------|--------------|
| `com.ddf.boot.mongo.config.MongoAutoConfiguration` | 自动配置         |
| `com.ddf.boot.mongo.helper.MongoTemplateHelper`    | MongoDB 操作工具 |

## 使用说明

### 1. 配置

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

### 2. 使用 MongoTemplate

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

### 3. 分页查询

```java
@Autowired
private MongoTemplateHelper mongoHelper;

// 分页查询
PageRequest pageRequest = PageRequest.of(0, 10);
Query query = new Query(Criteria.where("status").is(1));

Page<User> page = mongoHelper.handlerPage(query, User.class, pageRequest);
List<User> list = page.getContent();
long total = page.getTotalElements();
```

## 注意事项

1. **依赖注入**：本模块依赖外部项目提供 `MongoTemplate` Bean
2. **集合名**：默认使用类名作为集合名，可通过 `@Document(collection = "custom_name")` 指定
3. **索引管理**：建议在应用启动时创建必要的索引
