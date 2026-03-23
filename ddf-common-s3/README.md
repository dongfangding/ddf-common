# ddf-common-s3

S3 兼容对象存储通用模块，支持 MinIO、AWS S3、阿里云 OSS、腾讯云 COS 等兼容实现。

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
customizer:
  infra:
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
customizer:
  infra:
    s3:
      enable: true
      endpoint: https://s3.amazonaws.com
      access-key: AWS_ACCESS_KEY
      secret-key: AWS_SECRET_KEY
      session-token: AWS_SESSION_TOKEN
      bucket-name: prod-bucket
      region: us-east-1
      secure: true
      path-style-access: false
```

### 阿里云 OSS

```yaml
customizer:
  infra:
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
| `com.ddf.boot.common.s3.helper.FileUploadHelper` | 文件上传与缩略图辅助类 |
| `com.ddf.boot.common.s3.config.S3Properties` | S3 配置属性 |

## 使用示例

```java
@Autowired
private S3Api s3Api;

public String uploadFile(MultipartFile file) throws IOException {
    String objectKey = "images/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
    UploadResult result = s3Api.upload(
            objectKey,
            file.getInputStream(),
            file.getContentType(),
            file.getSize()
    );
    return result.getUrl();
}

public String getDownloadUrl(String objectKey) {
    PresignedUrlResult result = s3Api.getPresignedDownloadUrl(objectKey, Duration.ofHours(1));
    return result.getUrl();
}
```

## 注意事项

1. 配置前缀是 `customizer.infra.s3`，不是 `ddf.s3`。
2. `path-style-access=false` 时，模块会按虚拟主机风格生成对象访问地址。
3. 配置了 `session-token` 时，会通过 MinIO SDK 临时凭证能力参与签名。
4. 模块默认会在上传前检查 Bucket 是否存在，不存在时自动创建。
5. `FileUploadHelper` 默认只放行图片类型；如需文档、视频等类型，请显式配置 `allowed-file-types`。
