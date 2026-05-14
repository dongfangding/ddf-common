# ddf-common-alarm

> 统一告警通知模块。聚合钉钉、飞书等主流告警渠道，支持异常自动上报、定时扫描告警，
> 为业务系统提供标准化的告警出口。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-alarm` 解决的是 **"系统异常与关键事件如何及时触达运维/开发人员"** 问题。

| 场景     | 典型问题            | 模块提供的能力        |
|--------|-----------------|----------------|
| 生产异常告警 | 服务抛异常后无人感知，故障扩大 | 异常自动捕获并推送钉钉/飞书 |
| 定时任务监控 | 定时扫描任务失败无通知     | 扫描结果异常时触发告警    |
| 业务关键事件 | 订单积压、库存不足等需即时告知 | 业务代码主动调用告警 API |
| 多渠道覆盖  | 团队使用不同IM工具，告警分散 | 统一接口，渠道可配置切换   |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-alarm</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. 最小化配置

```yaml
ddf:
  alarm:
    enabled: true
    ding-talk:
      enabled: true
      webhook: https://oapi.dingtalk.com/robot/send?access_token=xxx
      secret: your-secret                      # 安全设置中的加签密钥
    lark:
      enabled: false
      webhook: https://open.feishu.cn/open-apis/bot/v2/hook/xxx
      secret: your-secret
    exception:
      enabled: true                            # 是否开启异常自动告警
      interval-seconds: 300                    # 同一异常的最小告警间隔（防刷屏）
```

---

## 4. 核心 API

### 4.1 主动发送告警

```java
@Autowired
private AlarmApi alarmApi;

public void notifyOps() {
    alarmApi.sendAlarm("订单服务告警", "订单积压超过 1000 笔，请检查消费端");
}
```

### 4.2 异常自动告警

开启 `ddf.alarm.exception.enabled=true` 后，模块会自动捕获未处理异常并推送告警，无需业务代码介入。

告警内容包含：异常类型、异常消息、堆栈摘要、发生时间、应用名。

### 4.3 Redis Key 规范

告警频率控制使用 Redis 缓存，Key 由 `AlarmRedisKeyEnum` 统一管理，遵循 `{applicationName}:alarm:{type}:{identifier}` 格式。

---

## 5. 进阶用法 / 扩展点

### 5.1 自定义告警渠道

实现 `AlarmApi` 接口并注册为 Spring Bean，即可扩展企业微信、短信等其他渠道：

```java
@Component
@ConditionalOnProperty(prefix = "ddf.alarm.wechat", name = "enabled", havingValue = "true")
public class WechatAlarmApi implements AlarmApi {
    @Override
    public void sendAlarm(String title, String content) {
        // 调用企业微信机器人 API
    }
}
```

### 5.2 业务层集成告警

在关键业务流程中埋点告警：

```java
@Service
public class OrderService {
    @Autowired
    private AlarmApi alarmApi;

    public void checkBacklog() {
        long backlog = orderMapper.countPending();
        if (backlog > 1000) {
            alarmApi.sendAlarm("订单积压告警", "当前积压: " + backlog);
        }
    }
}
```

### 5.3 告警频率控制

通过 `exception.interval-seconds` 控制同一异常的最小告警间隔，避免异常风暴导致消息刷屏。

频率控制基于 Redis，分布式部署下仍然有效。

---

## 6. 与其他模块协作

| 模块                              | 协作方式               |
|---------------------------------|--------------------|
| `ddf-common-redis`              | 告警频率控制依赖 Redis 缓存  |
| `ddf-common-governance-starter` | Mail 可作为告警渠道的兜底补充  |
| `ddf-common-core`               | 异常摘要、JSON 序列化等基础能力 |

---

## 7. FAQ

**Q1：异常告警会捕获所有异常吗？**
仅捕获未被业务层显式处理的运行时异常。建议在全局异常处理器中自行判断是否需要告警。

**Q2：告警频率控制基于什么？**
基于 Redis，Key 由异常类名 + 方法签名哈希生成，分布式多实例下共享同一控制窗口。

**Q3：钉钉机器人如何配置？**

1. 在钉钉群中添加自定义机器人
2. 复制 Webhook 地址和加签密钥
3. 填入 `ddf.alarm.ding-talk.webhook` 和 `ddf.alarm.ding-talk.secret`

**Q4：不配置任何渠道会怎样？**
`AlarmApi` 会降级为空实现（打印日志），不会阻塞业务流程。

---

## 8. 参考

- 源码：`AlarmAutoConfiguration`、`AlarmApi`、`AlarmProperties`
- 钉钉机器人文档：https://open.dingtalk.com/document/robots/custom-robot-access
- 飞书机器人文档：https://open.feishu.cn/document/ukTMukTMukTM/ucTM5YjL3ETO24yNxkjN
