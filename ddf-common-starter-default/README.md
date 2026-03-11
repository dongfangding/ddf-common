# ddf-common-starter-default

默认业务场景聚合 starter。

当前聚合模块：

- `ddf-common-starter-web`
- `ddf-common-data-mysql-starter`
- `ddf-common-governance-starter`

适用场景：

- 常规单体或微服务业务应用
- 需要 Web、数据库、治理能力的一般后端服务

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 提供能力

- Web 场景基础能力
- JDBC + MySQL + Druid 数据接入
- Mail + Actuator 治理能力

## 设计目标

- 业务应用优先只引一个 starter
- 基础库内部边界仍然保持清晰
- 治理层和数据库层可以独立演进

## 典型配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/demo
    username: root
    password: 123456
  mail:
    host: smtp.example.com
    username: no-reply@example.com
    password: your-password

customizer:
  governance:
    mail:
      enabled: true
```

说明：

- 不配置 `spring.mail.*` 时，引入 starter 仍然安全
- Mail 能力只有在底层 Bean 存在时才自动接回
