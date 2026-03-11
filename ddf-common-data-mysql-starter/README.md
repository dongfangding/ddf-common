# ddf-common-data-mysql-starter

MySQL 数据基础设施 starter。

当前聚合模块：

- `spring-boot-starter-jdbc`
- `mysql-connector-j`
- `druid-spring-boot-3-starter`

设计目标：

- 固化当前项目的数据接入技术栈
- 业务服务不手工拼装 `jdbc/mysql/druid`
- 把数据层配置统一收口到 starter

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-data-mysql-starter</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 配置说明

统一配置前缀：

```yaml
customizer:
  data:
    mysql:
      enabled: true
      druid:
        usePingMethod: false
```

说明：

- `customizer.data.mysql.enabled`
  - 默认 `true`
  - 允许注册该 starter 的数据层增强能力
- `customizer.data.mysql.druid.usePingMethod`
  - 默认 `false`
  - 对应 `druid.mysql.usePingMethod`

业务数据库配置仍然继续使用 Spring 标准配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/demo
    username: root
    password: 123456
```

## 推荐搭配

常规业务服务推荐搭配：

- `ddf-common-starter-web`
- `ddf-common-data-mysql-starter`

或者直接使用：

- `ddf-common-starter-default`
