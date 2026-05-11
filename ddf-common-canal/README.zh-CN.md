# ddf-common-canal

> Canal 数据库变更订阅模块。监听 MySQL binlog 并将 INSERT/UPDATE/DELETE 变更事件分发给业务处理器，
> 适用于数据同步、缓存刷新、异构数据构建等场景。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-canal` 解决的是 **"MySQL 数据变更如何被业务层实时感知"** 问题。

| 场景 | 典型问题 | 模块提供的能力 |
| --- | --- | --- |
| 缓存一致性 | 数据库更新了，缓存未刷新导致脏读 | 监听 binlog，数据变更后自动清缓存 |
| 异构数据同步 | MySQL 数据需同步到 ES / MongoDB | 捕获变更事件，异步写入目标存储 |
| 数据审计 | 需要记录谁在什么时间改了什么数据 | 监听变更，写入审计日志表 |
| 业务事件驱动 | 订单状态变更后触发后续流程 | 将数据库变更转化为业务事件 |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-canal</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. 最小化配置

```yaml
ddf:
  canal:
    server-host: localhost           # Canal 服务器地址
    port: 11111                      # Canal 端口
    destination: example             # Canal 实例名称
    username: canal                  # 用户名
    password: canal                  # 密码
```

> 使用本模块前需先部署 Canal Server 并配置对应的数据库连接。参考 Canal 官方文档进行部署。

---

## 4. 核心 API

### 4.1 实现消息处理器

```java
@Component
public class UserCanalHandler implements CanalMessageHandler<User> {

    @Override
    public boolean match(String tableName) {
        // 匹配关注的表名
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
                // 处理新增数据
            }
        } else if ("UPDATE".equals(type)) {
            for (User user : newData) {
                // 处理更新数据
            }
        } else if ("DELETE".equals(type)) {
            for (User user : oldData) {
                // 处理删除数据
            }
        }
    }

    @Override
    public ThreadPoolExecutor getExecutor() {
        // 返回自定义线程池实现异步处理；返回 null 则同步处理
        return null;
    }
}
```

### 4.2 消息格式

Canal 推送的 `FlatMessage` 结构：

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

## 5. 进阶用法 / 扩展点

### 5.1 异步处理

返回自定义线程池，避免 binlog 消费阻塞 Canal 客户端拉取：

```java
@Override
public ThreadPoolExecutor getExecutor() {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(
        4, 8, 60, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(1000),
        new ThreadFactoryBuilder().setNameFormat("canal-handler-%d").build()
    );
    return executor;
}
```

### 5.2 多表匹配

`match()` 方法支持模糊匹配，一个 Handler 可处理多张表：

```java
@Override
public boolean match(String tableName) {
    return tableName.startsWith("t_order");
}
```

### 5.3 结合 Redis 清缓存

```java
@Override
public void handle(FlatMessage message, List<User> oldData, List<User> newData) {
    for (User user : newData) {
        redisTemplate.delete("user:" + user.getId());
    }
}
```

---

## 6. 与其他模块协作

| 模块 | 协作方式 |
| --- | --- |
| `ddf-common-redis` | 数据变更后自动清理 Redis 缓存 |
| `ddf-common-es` | 数据变更后同步到 Elasticsearch 索引 |
| `ddf-common-core` | JSON 反序列化、线程池等基础能力 |

---

## 7. FAQ

**Q1：Canal 与 MySQL 主从复制有什么区别？**
Canal 模拟 MySQL Slave 协议读取 binlog，但不做数据复制，而是将变更事件解析后推送给业务消费者。

**Q2：消费端需要保证幂等吗？**
必须。Canal 可能因网络抖动或客户端重启导致消息重复推送，业务处理器需做好幂等控制。

**Q3：Canal 延迟有多大？**
通常毫秒级。但如果 Canal Server 或消费端处理慢，可能累积到秒级。

**Q4：支持 DDL 事件吗？**
当前模块主要处理 DML（INSERT/UPDATE/DELETE）。DDL 事件需额外配置 Canal Server。

---

## 8. 参考

- 源码：`CanalMessageDispatcher`、`CanalMessageHandler`
- Canal 官方文档：https://github.com/alibaba/canal
