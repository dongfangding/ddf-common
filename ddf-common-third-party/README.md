# ddf-common-third-party

> Third-party service integration module. Currently focused on Alibaba Cloud OSS and SMS services,
> providing reusable third-party API wrappers for business systems.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-third-party` solves the **"repeated development when integrating third-party cloud services"** problem.

| Scenario | Typical Problem | What the Module Provides |
| --- | --- | --- |
| File upload to object storage | Each business module integrates OSS independently with inconsistent SDK usage | Unified `OssApi` wrapper |
| SMS verification / notification | SMS signatures and template management scattered across businesses | Unified `SmsApi` wrapper |
| Multi-cloud migration | Moving from Alibaba Cloud to another cloud requires wide changes | Abstracted interface with replaceable implementations |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-third-party</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. Minimum Configuration

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
      sign-name: YourSmsSign
```

> In production, inject keys via environment variables or a config center; never hardcode them in configuration files.

---

## 4. Core API

### 4.1 OSS file upload

```java
@Autowired
private OssApi ossApi;

// Upload from stream
public String upload(String key, InputStream inputStream) {
    return ossApi.upload(key, inputStream);
}

// Get file access URL
public String getUrl(String key) {
    return ossApi.getUrl(key);
}

// Delete file
public void delete(String key) {
    ossApi.delete(key);
}
```

### 4.2 Send SMS

```java
@Autowired
private SmsApi smsApi;

// Send verification code
public void sendVerifyCode(String phone, String code) {
    smsApi.sendSms(phone, "SMS_TEMPLATE_CODE", Map.of("code", code));
}
```

---

## 5. Advanced Usage / Extension Points

### 5.1 Custom storage strategy

Implement `OssApi` to integrate other cloud vendors (e.g. Tencent COS, AWS S3):

```java
@Component
@ConditionalOnProperty(prefix = "ddf.third-party.oss", name = "provider", havingValue = "cos")
public class CosOssApi implements OssApi {
    // Tencent COS implementation
}
```

### 5.2 Batch upload

```java
List<String> keys = files.stream()
    .map(file -> ossApi.upload(UUID.randomUUID().toString(), file.getInputStream()))
    .collect(Collectors.toList());
```

---

## 6. Interplay with Other Modules

| Module | How They Cooperate |
| --- | --- |
| `ddf-common-core` | JSON, Map, string utilities, and other fundamentals |
| `ddf-common-captcha` | SMS verification code sending can call `SmsApi` |

---

## 7. FAQ

**Q1: Are other cloud vendors supported?**
The default implementation targets Alibaba Cloud. Other vendors can be supported by implementing the `OssApi` / `SmsApi` interfaces.

**Q2: What to do if credentials are leaked?**
Immediately disable the corresponding AccessKey in the cloud console and rotate to new keys. Never commit credentials to a code repository.

**Q3: Is there a size limit for OSS uploads?**
Limited by OSS server-side and client-side configuration. For large files, multipart upload is recommended.

---

## 8. References

- Source: `OssApi`, `OssHelper`, `OssProperties`, `AliYunSmsProperties`
- Alibaba Cloud OSS docs: https://help.aliyun.com/product/31815.html
