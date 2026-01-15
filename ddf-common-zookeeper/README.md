# ddf-common-zookeeper

Zookeeper 服务发现和配置管理模块。

## 功能特性

- 服务注册发现
- 配置监听
- 节点管理

## 依赖引入

```xml
<dependency>
    <groupId>com.ddf.common</groupId>
    <artifactId>ddf-common-zookeeper</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心类

| 类路径                | 功能     |
|--------------------|--------|
| `ZkClient`         | ZK 客户端 |
| `ServiceDiscovery` | 服务发现   |
| `ZkProperties`     | 配置属性   |

## 使用说明

### 配置

```yaml
ddf:
  zookeeper:
    host: localhost:2181
    session-timeout: 6000
    connection-timeout: 3000
```

### 服务注册

```java
@Autowired
private ServiceDiscovery serviceDiscovery;

public void register(String serviceName, String address) {
    serviceDiscovery.register(serviceName, address);
}
```

### 服务发现

```java
public List<String> getAddresses(String serviceName) {
    return serviceDiscovery.getAddresses(serviceName);
}
```
