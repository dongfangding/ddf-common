# ddf-common-canal

> Canal database change subscription module. Listens to MySQL binlog and dispatches INSERT/UPDATE/DELETE change events to business handlers,
> suitable for data synchronization, cache refreshing, and heterogenous data construction scenarios.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-canal` solves the **"how to make business layers aware of MySQL data changes in real time"** problem.

| Scenario                | Typical Problem                                               | What the Module Provides                                 |
|-------------------------|---------------------------------------------------------------|----------------------------------------------------------|
| Cache consistency       | Database updated but cache not refreshed, causing stale reads | Listen to binlog and auto-clear cache after data changes |
| Heterogeneous data sync | MySQL data needs to sync to ES / MongoDB                      | Capture change events and async-write to target storage  |
| Data audit              | Need to record who changed what and when                      | Listen to changes and write to audit log tables          |
| Business event driving  | Order status change triggers downstream flow                  | Transform DB changes into business events                |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-canal</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. Minimum Configuration

```yaml
ddf:
  canal:
    server-host: localhost
    port: 11111
    destination: example
    username: canal
    password: canal
```

> Before using this module, deploy a Canal Server and configure the corresponding database connection.
> Refer to the Canal official documentation for deployment instructions.

---

## 4. Core API

### 4.1 Implement a message handler

```java
@Component
public class UserCanalHandler implements CanalMessageHandler<User> {

    @Override
    public boolean match(String tableName) {
        return "t_user".equalsIgnoreCase(tableName);
    }

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public void handle(FlatMessage message, List<User> oldData, List<User> newData) {
        String type = message.getType();  // INSERT / UPDATE / DELETE

        if ("INSERT".equals(type)) {
            for (User user : newData) {
                // Handle insert
            }
        } else if ("UPDATE".equals(type)) {
            for (User user : newData) {
                // Handle update
            }
        } else if ("DELETE".equals(type)) {
            for (User user : oldData) {
                // Handle delete
            }
        }
    }

    @Override
    public ThreadPoolExecutor getExecutor() {
        // Return a custom thread pool for async processing; null means synchronous
        return null;
    }
}
```

### 4.2 Message format

The `FlatMessage` structure pushed by Canal:

```json
{
  "table": "t_user",
  "type": "UPDATE",
  "data": [
    {"id": 1, "name": "newName", "age": 25}
  ],
  "old": [
    {"id": 1, "name": "oldName", "age": 24}
  ]
}
```

---

## 5. Advanced Usage / Extension Points

### 5.1 Async processing

Return a custom thread pool to avoid blocking the Canal client pull:

```java
@Override
public ThreadPoolExecutor getExecutor() {
    return new ThreadPoolExecutor(
        4, 8, 60, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(1000),
        new ThreadFactoryBuilder().setNameFormat("canal-handler-%d").build()
    );
}
```

### 5.2 Multi-table matching

The `match()` method supports fuzzy matching; one handler can process multiple tables:

```java
@Override
public boolean match(String tableName) {
    return tableName.startsWith("t_order");
}
```

### 5.3 Combine with Redis cache clearing

```java
@Override
public void handle(FlatMessage message, List<User> oldData, List<User> newData) {
    for (User user : newData) {
        redisTemplate.delete("user:" + user.getId());
    }
}
```

---

## 6. Interplay with Other Modules

| Module             | How They Cooperate                                         |
|--------------------|------------------------------------------------------------|
| `ddf-common-redis` | Auto-clear Redis cache after data changes                  |
| `ddf-common-es`    | Sync data changes to Elasticsearch indices                 |
| `ddf-common-core`  | JSON deserialization, thread pools, and other fundamentals |

---

## 7. FAQ

**Q1: What's the difference between Canal and MySQL replication?**
Canal simulates the MySQL Slave protocol to read binlog, but instead of replicating data, it parses change events and pushes them to business consumers.

**Q2: Does the consumer need to be idempotent?**
Yes. Canal may re-push messages due to network jitter or client restarts; business handlers must implement idempotency.

**Q3: How large is the Canal latency?**
Usually millisecond-level. However, if the Canal Server or consumer is slow, it may accumulate to seconds.

**Q4: Are DDL events supported?**
This module primarily handles DML (INSERT/UPDATE/DELETE). DDL events require additional Canal Server configuration.

---

## 8. References

- Source: `CanalMessageDispatcher`, `CanalMessageHandler`
- Canal docs: https://github.com/alibaba/canal
