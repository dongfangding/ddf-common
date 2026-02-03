# ddf-common-third-party

第三方服务集成模块，提供 OSS、SMS 等第三方服务封装。

## 功能特性

- OSS 对象存储
- SMS 短信服务
- 统一接口封装

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-third-party</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心类

| 类路径 | 功能 |
|-------|------|
| `com.ddf.boot.common.third.party.oss.OssApi` | OSS 操作接口 |
| `com.ddf.boot.common.third.party.sms.SmsApi` | 短信发送接口 |
| `com.ddf.boot.common.third.party.config.ThirdPartyProperties` | 配置属性 |

## 使用说明

### OSS 配置

```yaml
ddf:
  third-party:
    oss:
      endpoint: oss-cn-hangzhou.aliyuncs.com
      access-key-id: your-access-key
      access-key-secret: your-access-secret
      bucket-name: your-bucket
```

### 上传文件

```java
@Autowired
private OssApi ossApi;

public String upload(String key, InputStream inputStream) {
    return ossApi.upload(key, inputStream);
}
```

## 注意事项

1. **密钥安全**：生产环境使用配置中心管理密钥
2. **权限控制**：合理设置 Bucket 权限
