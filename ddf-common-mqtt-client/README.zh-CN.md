# ddf-common-mqtt-client

> 基于 `ddf-common-mqtt` 的上层业务封装模块。提供更贴近业务的 Topic 定义、消息体抽象和 REST 控制器，
> 适合需要在业务层快速集成 MQTT 发布能力的场景。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-mqtt-client` 解决的是 **"业务层如何快速、标准化地使用 MQTT 发布消息"** 问题。

| 场景            | 典型问题                         | 模块提供的能力                                |
|---------------|------------------------------|----------------------------------------|
| 业务系统推送设备指令    | 不想直接操作底层 MQTT API            | 控制器封装，HTTP 调用即发布                       |
| 多业务线 Topic 管理 | Topic 命名混乱、难以维护              | `MqttTopicDefine` 枚举化定义                |
| 消息体结构化        | 直接传 JSON 字符串易出错              | `TextMessageBody` 等业务消息体封装             |
| 与现有 Web 服务集成  | 已有 Spring MVC 接口，想扩展 MQTT 能力 | 自动注入 `MqttClientController` 暴露 REST 端点 |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-mqtt-client</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> 本模块已传递依赖 `ddf-common-mqtt`，无需重复引入。

---

## 3. 最小化配置

底层连接复用 `ddf-common-mqtt` 的配置，业务层仅需关注 Topic 和消息体定义：

```yaml
customizer:
  infra:
    mqtt:
      enable: true
      server-host: localhost
      server-port: 1883
      client-id: "ddf-client-001"
      username: admin
      password: password
```

---

## 4. 核心 API

### 4.1 定义业务 Topic

使用 `MqttTopicDefine` 枚举统一管理 Topic：

```java
public enum BizMqttTopic implements MqttTopicDefine {
    DEVICE_COMMAND("device/{deviceId}/command"),
    DEVICE_STATUS("device/{deviceId}/status"),
    ALERT_NOTIFICATION("alert/notification");

    private final String topic;

    BizMqttTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String getTopic() {
        return topic;
    }
}
```

### 4.2 构建业务消息体

```java
TextMessageBody message = TextMessageBody.builder()
    .topic(BizMqttTopic.DEVICE_COMMAND.getTopic())
    .content("{\"action\":\"reboot\",\"delay\":0}")
    .build();
```

### 4.3 通过控制器发布（REST 调用）

模块自动暴露 `MqttClientController`，支持通过 HTTP 请求触发 MQTT 发布：

```java
@Autowired
private MqttClientController mqttClientController;

// 发送设备重启指令
MqttMessageRequest request = MqttMessageRequest.builder()
    .topic("device/001/command")
    .payLoad("{\"action\":\"reboot\"}")
    .qos(1)
    .async(true)
    .build();

ResponseData<?> response = mqttClientController.publish(request);
```

### 4.4 直接调用底层客户端

如需更灵活的控制，仍可注入基础模块的 `MqttPublishClient`：

```java
@Autowired
private MqttPublishClient mqttClient;

ResponseData<MqttMessageResponse> response = mqttClient.publish(
    InnerMqttMessageRequest.builder()
        .topic(BizMqttTopic.ALERT_NOTIFICATION.getTopic())
        .payLoad(alertData)
        .control(MqttMessageControl.builder()
            .async(false)
            .qos(MqttQosEnum.AT_LAST_ONCE)
            .build())
        .build()
);
```

---

## 5. 进阶用法 / 扩展点

### 5.1 自定义消息体类型

继承或实现模块提供的消息体接口，扩展业务专属格式：

```java
@Data
@Builder
public class DeviceCommandBody implements MqttMessageBody {
    private String deviceId;
    private String action;
    private Map<String, Object> params;

    @Override
    public String toPayload() {
        return JsonUtil.toJson(this);
    }
}
```

### 5.2 Topic 模板参数替换

支持 `{变量}` 占位符动态替换：

```java
String topic = BizMqttTopic.DEVICE_COMMAND.getTopic()
    .replace("{deviceId}", "DEV-001");
```

---

## 6. 与其他模块协作

| 模块                | 协作方式                         |
|-------------------|------------------------------|
| `ddf-common-mqtt` | 底层 MQTT 连接与发布能力，本模块在其之上做业务封装 |
| `ddf-common-core` | JSON 序列化、工具类支撑               |
| `ddf-common-mvc`  | REST 控制器暴露与统一响应格式            |

---

## 7. FAQ

**Q1：本模块与 `ddf-common-mqtt` 有什么区别？**
`ddf-common-mqtt` 是协议层封装（连接、发布、QoS）；本模块是业务层封装（Topic 定义、消息体模型、REST 控制器），两者是上下层关系。

**Q2：是否支持 MQTT 订阅消费？**
当前默认实现聚焦发布链路。订阅消费建议在业务层基于 Paho 客户端自行扩展，或结合 `ddf-common-mqtt` 的订阅能力实现。

**Q3：控制器暴露的端点需要做权限控制吗？**
建议在生产环境对 `MqttClientController` 的端点增加鉴权或限制内网访问，避免未授权的消息发布。

---

## 8. 参考

- 源码：`MqttClientController`、`MqttMessageRequest`、`MqttTopicDefine`、`TextMessageBody`
- 依赖模块：`ddf-common-mqtt`
