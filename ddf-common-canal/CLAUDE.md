# CLAUDE.md

## 模块简介

提供 Canal 数据库变更订阅和分发功能，用于监听 MySQL binlog 实现数据同步。

## 核心类

| 类路径                                           | 功能      |
|-----------------------------------------------|---------|
| `com.ddf.common.canal.CanalMessageDispatcher` | 消息分发器   |
| `com.ddf.common.canal.CanalMessageHandler<T>` | 消息处理器接口 |

## 使用说明

### 1. 实现消息处理器

```java
@Component
public class UserCanalHandler implements CanalMessageHandler<User> {

    @Override
    public boolean match(String tableName) {
        // 匹配表名
        return "t_user".equalsIgnoreCase(tableName);
    }

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public void handle(FlatMessage message, List<User> oldData, List<User> newData) {
        // 处理变更数据
        String type = message.getType();  // INSERT/UPDATE/DELETE

        if ("INSERT".equals(type)) {
            for (User user : newData) {
                // 新增处理
            }
        } else if ("UPDATE".equals(type)) {
            for (User user : newData) {
                // 更新处理
            }
        } else if ("DELETE".equals(type)) {
            for (User user : oldData) {
                // 删除处理
            }
        }
    }

    @Override
    public ThreadPoolExecutor getExecutor() {
        // 可选：返回自定义线程池
        return null;  // null 表示同步处理
    }
}
```

### 2. 配置 Canal

```yaml
ddf:
  canal:
    server-host: localhost           # Canal 服务器地址
    port: 11111                      # Canal 端口
    destination: example             # 实例名称
    username: canal                  # 用户名
    password: canal                  # 密码
```

### 3. 消息格式

Canal 发送的 `FlatMessage` 包含：

```json
{
  "table": "t_user",           // 表名
  "type": "UPDATE",            // 操作类型
  "data": [                    // 变更后数据
    {"id": 1, "name": "newName", "age": 25}
  ],
  "old": [                     // 变更前数据
    {"id": 1, "name": "oldName", "age": 24}
  ]
}
```

## 注意事项

1. **表匹配**：`match()` 方法用于匹配表名，支持模糊匹配
2. **线程池**：返回自定义 `ThreadPoolExecutor` 实现异步处理
3. **幂等处理**：消费端必须做好幂等，防止重复消费
4. **数据延迟**：Canal 存在毫秒级延迟，实时性要求高的场景需注意
