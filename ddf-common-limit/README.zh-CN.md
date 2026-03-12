# ddf-common-limit

[English](./README.md) | [中文](./README.zh-CN.md)

限流与防重复提交模块。

定位：

- 提供接口限流、防重复提交等能力
- 作为 `ddf-common-starter-web` 的组成部分
- 依赖认证、MVC、Redis 等基础能力共同工作

## 当前提供能力

- `@RateLimit`
- `@MultiRateLimit`
- `@EnableRateLimit`
- `@EnableRepeatable`
- 防重复提交能力
- 限流 Key 生成扩展

## 使用建议

不建议业务项目直接依赖 `ddf-common-limit`。

推荐方式：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

如果业务同时需要数据库和治理能力，推荐直接使用：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 说明

`ddf-common-limit` 当前仍然和：

- `ddf-common-mvc`
- `ddf-common-authentication`
- `ddf-common-redis`

存在协同关系，因此更适合作为 starter 内部组成部分使用，而不是由业务项目手工单独拼装。

补充说明：

- 当前 `LimitAutoConfiguration` 本身较轻量
- 真正的限流与防重复提交能力启用，仍主要依赖 `@EnableRateLimit`、`@EnableRepeatable` 导入注册链路
- 因此仅仅引入依赖，并不等价于所有限流能力都会自动生效
