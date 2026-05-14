# CLAUDE.md

## 模块简介

S3 兼容对象存储模块，支持 MinIO、AWS S3、阿里云 OSS 等兼容实现。

## 核心类

| 类路径                                              | 功能       |
|--------------------------------------------------|----------|
| `com.ddf.boot.common.s3.api.S3Api`               | S3 操作接口  |
| `com.ddf.boot.common.s3.service.S3Service`       | S3 服务实现  |
| `com.ddf.boot.common.s3.helper.S3Helper`         | S3 操作辅助类 |
| `com.ddf.boot.common.s3.helper.FileUploadHelper` | 文件上传辅助类  |
| `com.ddf.boot.common.s3.config.S3Properties`     | S3 配置属性  |

## 配置

```yaml
customizer:
  infra:
    s3:
      enable: true
      endpoint: http://localhost:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket-name: default-bucket
      region: us-east-1
```

## 使用说明

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
```

## 切换存储服务

只需要修改配置即可切换兼容实现：

- MinIO：`endpoint: http://localhost:9000`
- AWS S3：`endpoint: https://s3.amazonaws.com`，`path-style-access: false`
- 阿里云 OSS：`endpoint: https://oss-cn-hangzhou.aliyuncs.com`

## 注意事项

1. 配置前缀是 `customizer.infra.s3`。
2. `path-style-access=false` 时使用虚拟主机风格访问。
3. 上传时如果 Bucket 不存在，模块会自动创建。
4. 配置了 `session-token` 时，模块会按临时凭证方式参与签名。
