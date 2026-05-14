# ddf-common-third-party

> 第三方服务集成模块。目前聚焦阿里云 OSS 和短信服务，为业务系统提供可复用的第三方接口封装。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-third-party` 解决的是 **"第三方云服务接入的重复开发"** 问题。

| 场景        | 典型问题                  | 模块提供的能力        |
|-----------|-----------------------|----------------|
| 文件上传至对象存储 | 各业务各自对接 OSS，SDK 用法不一致 | 统一 `OssApi` 封装 |
| 短信验证码/通知  | 短信签名、模板管理散落在各业务       | 统一 `SmsApi` 封装 |
| 多云厂商切换    | 从阿里云迁到其他云，改动面大        | 抽象接口，底层可替换     |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-third-party</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. 最小化配置

```yaml
ddf:
  third-party:
    oss:
      endpoint: oss-cn-hangzhou.aliyuncs.com
      access-key-id: ${OSS_ACCESS_KEY_ID}
      access-key-secret: ${OSS_ACCESS_KEY_SECRET}
      bucket-name: my-bucket
    sms:
      access-key-id: ${SMS_ACCESS_KEY_ID}
      access-key-secret: ${SMS_ACCESS_KEY_SECRET}
      sign-name: 你的短信签名
```

> 生产环境密钥建议通过环境变量或配置中心注入，不要硬编码在配置文件中。

---

## 4. 核心 API

### 4.1 OSS 文件上传

```java
@Autowired
private OssApi ossApi;

// 上传文件流
public String upload(String key, InputStream inputStream) {
    return ossApi.upload(key, inputStream);
}

// 获取文件访问 URL
public String getUrl(String key) {
    return ossApi.getUrl(key);
}

// 删除文件
public void delete(String key) {
    ossApi.delete(key);
}
```

### 4.2 短信发送

```java
@Autowired
private SmsApi smsApi;

// 发送验证码
public void sendVerifyCode(String phone, String code) {
    smsApi.sendSms(phone, "SMS_TEMPLATE_CODE", Map.of("code", code));
}
```

---

## 5. 进阶用法 / 扩展点

### 5.1 自定义存储策略

实现 `OssApi` 接口可对接其他云厂商（如腾讯云 COS、AWS S3）：

```java
@Component
@ConditionalOnProperty(prefix = "ddf.third-party.oss", name = "provider", havingValue = "cos")
public class CosOssApi implements OssApi {
    // 腾讯云 COS 实现
}
```

### 5.2 批量上传

```java
List<String> keys = files.stream()
    .map(file -> ossApi.upload(UUID.randomUUID().toString(), file.getInputStream()))
    .collect(Collectors.toList());
```

---

## 6. 与其他模块协作

| 模块                   | 协作方式                |
|----------------------|---------------------|
| `ddf-common-core`    | JSON、Map、字符串等工具支撑   |
| `ddf-common-captcha` | 短信验证码发送可调用 `SmsApi` |

---

## 7. FAQ

**Q1：是否支持其他云厂商？**
当前默认实现针对阿里云。可通过实现 `OssApi` / `SmsApi` 接口扩展其他厂商。

**Q2：密钥泄露如何处理？**
立即在云平台控制台禁用对应 AccessKey，并轮换新密钥。切勿将密钥提交到代码仓库。

**Q3：OSS 上传有大小限制吗？**
受限于 OSS 服务端和客户端配置。大文件建议使用分片上传。

---

## 8. 参考

- 源码：`OssApi`、`OssHelper`、`OssProperties`、`AliYunSmsProperties`
- 阿里云 OSS 文档：https://help.aliyun.com/product/31815.html
