# ddf-common-governance-starter

治理层基础 starter，当前聚合：

- Mail
- Actuator

设计目标：

- 引入后默认安全
- 未配置 mail 时不报错
- 已配置底层能力时自动接回治理能力
- 后续告警、审计、监控扩展统一往治理层收口

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-governance-starter</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 配置说明

治理层统一配置前缀：

```yaml
customizer:
  governance:
    mail:
      enabled: true
    observability:
      enabled: true
```

说明：

- `customizer.governance.mail.enabled`
  - 默认 `true`
  - 表示允许注册治理层邮件能力
  - 但不会强制 mail 启动
- 如果业务没有配置 `spring.mail.*`
  - starter 可以安全引入
  - mail 相关 Bean 不会被注册
- 如果业务配置了 `spring.mail.*` 且 `mail.enabled=true`
  - 会自动注册 `MailService`

## 当前能力

- `MailService`
  - 治理层正式邮件抽象
- `MailUtil`
  - 兼容旧用法的静态入口
  - 底层通过 `MailService` 延迟调用，不在类加载阶段强取 Bean

## 后续规划

- 告警通知
- 健康检查扩展
- 指标与线程池治理
- 审计与应用元信息
