# ddf-common-canal

Canal 数据库变更订阅模块，用于监听 MySQL binlog 实现数据同步。

## 功能特性

- 数据库变更监听
- 消息分发处理
- 支持 INSERT/UPDATE/DELETE

## 依赖引入

```xml
<dependency>
    <groupId>com.ddf.common</groupId>
    <artifactId>ddf-common-canal</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心类

| 类路径                      | 功能      |
|--------------------------|---------|
| `CanalMessageDispatcher` | 消息分发器   |
| `CanalMessageHandler<T>` | 消息处理器接口 |

## 使用说明

实现 `CanalMessageHandler` 接口处理数据变更：

```java
@Component
public class UserCanalHandler implements CanalMessageHandler<User> {
    @Override
    public boolean match(String tableName) {
        return "t_user".equalsIgnoreCase(tableName);
    }

    @Override
    public void handle(FlatMessage message, List<User> oldData, List<User> newData) {
        // 处理变更数据
    }
}
```

## 配置

```yaml
ddf:
  canal:
    server-host: localhost
    port: 11111
    destination: example
```
