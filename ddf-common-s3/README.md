# ddf-common-s3

S3 兼容存储通用模块，支持 MinIO、AWS S3、阿里云 OSS、腾讯云 COS 等。

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-s3</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 配置说明

### MinIO 本地环境

```yaml
ddf:
  s3:
    enable: true
    endpoint: http://localhost:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket-name: my-bucket
    region: us-east-1
    secure: false
    path-style-access: true
```

### AWS S3 生产环境

```yaml
ddf:
  s3:
    enable: true
    endpoint: https://s3.amazonaws.com
    access-key: AWS_ACCESS_KEY
    secret-key: AWS_SECRET_KEY
    bucket-name: prod-bucket
    region: us-east-1
    secure: true
    path-style-access: false  # AWS S3 推荐使用虚拟主机风格
```

### 阿里云 OSS

```yaml
ddf:
  s3:
    enable: true
    endpoint: https://oss-cn-hangzhou.aliyuncs.com
    access-key: OSS_ACCESS_KEY
    secret-key: OSS_SECRET_KEY
    bucket-name: oss-bucket
    region: cn-shanghai
    secure: true
```

## 核心类

| 类路径 | 功能 |
|-------|------|
| `com.ddf.boot.common.s3.api.S3Api` | S3 操作接口 |
| `com.ddf.boot.common.s3.service.S3Service` | S3 服务实现 |
| `com.ddf.boot.common.s3.helper.S3Helper` | S3 操作辅助类 |
| `com.ddf.boot.common.s3.config.S3Properties` | S3 配置属性 |

## 使用示例

```java
@Autowired
private S3Api s3Api;

public String uploadFile(MultipartFile file) {
    String objectKey = "images/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
    UploadResult result = s3Api.upload(objectKey, file.getInputStream(),
        file.getContentType(), file.getSize());
    return result.getUrl();
}

// 生成预签名下载 URL
public String getDownloadUrl(String objectKey) {
    PresignedUrlResult result = s3Api.getPresignedDownloadUrl(objectKey, Duration.ofHours(1));
    return result.getUrl();
}
```

## S3 兼容性

| 存储服务 | 兼容性 | 说明 |
|---------|-------|------|
| MinIO | 完整支持 | 开发测试环境推荐 |
| AWS S3 | 完整支持 | 生产环境推荐 |
| 阿里云 OSS | 完整支持 | 需要设置正确的 endpoint |
| 腾讯云 COS | 完整支持 | 需要设置正确的 endpoint |

## 注意事项

1. **配置前缀**: 使用 `customizer.infra.s3` 而不是 `customizer.infra.minio`
2. **path-style-access**: AWS S3 推荐设为 `false`（虚拟主机风格）
3. **Bucket 管理**: 上传文件时，如果 Bucket 不存在会自动创建
4. **Session Token**: 如需使用 AWS 临时凭证，请考虑使用 AWS SDK
