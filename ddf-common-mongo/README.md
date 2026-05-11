# ddf-common-mongo

> MongoDB integration helper module. Provides `MongoTemplate` operation wrappers and pagination utilities,
> serving as a shared organizational entry for MongoDB-related capabilities.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-mongo` solves the **"quick access to common MongoDB operations"** problem.

| Scenario | Typical Problem | What the Module Provides |
| --- | --- | --- |
| Document-oriented storage | Need flexible schema; relational DB is not suitable | `MongoTemplate` wrapper and pagination helpers |
| Log / event storage | High-volume semi-structured data writes | High-throughput writes via MongoDB |
| Existing MongoDB cluster | Business needs quick integration | Auto-configuration + utilities to reduce boilerplate |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-mongo</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. Minimum Configuration

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

## 4. Core API

### 4.1 MongoTemplate basic operations

This module imports `spring-boot-starter-data-mongodb`; the `MongoTemplate` bean is available automatically:

```java
@Autowired
private MongoTemplate mongoTemplate;

// Insert document
User user = new User("name", 25);
mongoTemplate.insert(user);

// Query document
Query query = new Query(Criteria.where("name").is("name"));
User found = mongoTemplate.findOne(query, User.class);

// Update document
Update update = new Update().set("age", 26);
UpdateResult result = mongoTemplate.updateFirst(query, update, User.class);

// Delete document
DeleteResult result = mongoTemplate.remove(query, User.class);
```

### 4.2 Paginated queries

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

## 5. Advanced Usage / Extension Points

### 5.1 Custom collection name

The default collection name is the class name. Use `@Document` to customize:

```java
@Document(collection = "custom_users")
public class User {
    @Id
    private String id;
    private String name;
}
```

### 5.2 Index management

It is recommended to create necessary indexes at application startup:

```java
@PostConstruct
public void initIndexes() {
    mongoTemplate.indexOps(User.class).ensureIndex(
        new Index().on("name", Sort.Direction.ASC).unique()
    );
}
```

### 5.3 Complex aggregation queries

Use `MongoTemplate`'s `aggregate` method directly:

```java
Aggregation aggregation = Aggregation.newAggregation(
    Aggregation.match(Criteria.where("status").is(1)),
    Aggregation.group("category").sum("amount").as("total"),
    Aggregation.sort(Sort.Direction.DESC, "total")
);
AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "orders", Document.class);
```

---

## 6. Interplay with Other Modules

| Module | How They Cooperate |
| --- | --- |
| `ddf-common-core` | JSON utilities, date handling, and other fundamentals |
| `ddf-common-starter-web` | Web controller layer receives requests and writes to MongoDB |

---

## 7. FAQ

**Q1: Does this module include a full Repository abstraction?**
Currently it is mainly `MongoTemplate` operation helpers and pagination utilities. Fuller Repository, query-model, and paging capabilities should be built in business code or later dedicated modules.

**Q2: Who provides the `MongoTemplate` bean?**
It is auto-configured by `spring-boot-starter-data-mongodb`. This module only provides operation wrappers and does not replace Spring Data MongoDB.

**Q3: Are MongoDB transactions supported?**
Yes. Spring Data MongoDB provides `@Transactional` support, but it requires a MongoDB replica set deployment.

---

## 8. References

- Source: `MongoAutoConfiguration`, `MongoTemplateHelper`
- Spring Data MongoDB: https://docs.spring.io/spring-data/mongodb/reference/
