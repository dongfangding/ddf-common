# ddf-common-starter-web

Web 场景聚合 starter。

当前聚合模块：

- `ddf-common-api`
- `ddf-common-core`
- `ddf-common-mvc`
- `ddf-common-authentication`
- `ddf-common-limit`

适用场景：

- 不需要数据库和治理能力的轻量 Web 服务
- 需要统一 MVC、认证、限流基础能力的业务服务

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 提供能力

- 通用 API、异常、DTO、约束
- Core 公共工具与 Spring 支撑
- MVC 基础配置
- 认证能力
- 限流与防重复提交

## 不包含的能力

- JDBC / MySQL / Druid
- Mail / Actuator / 治理扩展

如果需要数据库能力，继续引入：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-data-mysql-starter</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

如果需要治理能力，继续引入：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-governance-starter</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 推荐使用方式

轻量 Web 服务：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```
