# ddf-common-s3

> S3 兼容对象存储通用模块。基于 MinIO SDK 统一封装，支持 MinIO、AWS S3、阿里云 OSS、腾讯云 COS 等兼容实现，
> 提供文件上传、预签名 URL、缩略图生成和 Bucket 自动管理能力。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-s3` 解决的是 **"业务系统需要统一接入多种对象存储服务"** 问题。

| 场景        | 典型问题               | 模块提供的能力                                       |
|-----------|--------------------|-----------------------------------------------|
| 本地开发测试    | 不想依赖外部云存储          | MinIO 本地部署，配置即连                               |
| 生产环境切换云厂商 | 不同厂商 SDK 差异大，迁移成本高 | 统一 `S3Api` 接口，切换仅需改配置                         |
| 图片上传与预览   | 需要缩略图、预签名访问        | `FileUploadHelper` 自动生成缩略图，`S3Api` 生成临时访问 URL |
| 临时文件分享    | 私有 Bucket 文件需要限时访问 | 预签名下载链接，到期自动失效                                |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-s3</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. 最小化配置

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

---

## 4. 核心 API

### 4.1 文件上传

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

### 4.2 获取预签名下载链接

```java
public String getDownloadUrl(String objectKey) {
    // 链接 1 小时后过期
    PresignedUrlResult result = s3Api.getPresignedDownloadUrl(objectKey, Duration.ofHours(1));
    return result.getUrl();
}
```

### 4.3 图片上传与缩略图

```java
@Autowired
private FileUploadHelper fileUploadHelper;

public UploadResult uploadWithThumbnail(MultipartFile image) throws IOException {
    // 自动校验图片类型、生成缩略图、上传原图和缩略图
    return fileUploadHelper.uploadImage(image, "products/");
}
```

### 4.4 删除对象

```java
s3Api.delete(objectKey);
```

---

## 5. 进阶用法 / 扩展点

### 5.1 切换存储服务

只需修改配置即可切换兼容实现，业务代码零改动：

| 服务商     | endpoint 示例                             | path-style-access |
|---------|-----------------------------------------|-------------------|
| MinIO   | `http://localhost:9000`                 | `true`            |
| AWS S3  | `https://s3.amazonaws.com`              | `false`           |
| 阿里云 OSS | `https://oss-cn-hangzhou.aliyuncs.com`  | `false`           |
| 腾讯云 COS | `https://cos.ap-guangzhou.myqcloud.com` | `false`           |

### 5.2 Bucket 自动创建

模块默认会在上传前检查 Bucket 是否存在，不存在时自动创建。如需关闭：

```yaml
customizer:
  infra:
    s3:
      auto-create-bucket: false
```

### 5.3 文件类型白名单

`FileUploadHelper` 默认只放行图片类型；如需支持文档、视频等，显式配置：

```yaml
customizer:
  infra:
    s3:
      allowed-file-types: jpg,jpeg,png,gif,pdf,doc,docx,mp4
```

### 5.4 临时凭证（Session Token）

配置了 `session-token` 时，模块会按临时凭证方式参与签名，适用于 STS 授权场景：

```yaml
customizer:
  infra:
    s3:
      access-key: TEMP_ACCESS_KEY
      secret-key: TEMP_SECRET_KEY
      session-token: TEMP_SESSION_TOKEN
```

---

## 6. 与其他模块协作

| 模块                | 协作方式                                            |
|-------------------|-------------------------------------------------|
| `ddf-common-vps`  | VPS 模块提供 FastDFS 文件存储；S3 模块提供云对象存储，可按场景二选一或组合使用 |
| `ddf-common-core` | JSON 序列化、工具类支撑                                  |
| `ddf-common-api`  | `UploadResult`、`PresignedUrlResult` 等响应 DTO 定义  |

---

## 7. FAQ

**Q1：配置前缀为什么是 `customizer.infra.s3` 而不是 `ddf.s3`？**
项目统一使用 `customizer.infra.<feature>` 作为基础设施配置前缀，与 starter 自动配置风格保持一致。

**Q2：`path-style-access` 有什么作用？**
`true` 时使用路径风格访问（`http://endpoint/bucket/object`）；`false` 时使用虚拟主机风格（`http://bucket.endpoint/object`）。MinIO 通常需要 `true`，AWS S3 需要 `false`。

**Q3：预签名 URL 的有效期最长多久？**
取决于具体存储服务端限制，通常建议不超过 7 天。如需更长期限，考虑使用公开 Bucket 或配合 CDN。

**Q4：大文件分片上传是否支持？**
当前模块封装的是常规单文件上传接口。如需分片上传，建议直接使用 MinIO SDK 的 `composeObject` 或 AWS S3 的 Multipart Upload API。

---

## 8. 参考

- 源码：`S3Api`、`S3Service`、`S3Helper`、`FileUploadHelper`、`S3Properties`
- MinIO Java SDK：https://min.io/docs/minio/linux/developers/java/minio-java.html
