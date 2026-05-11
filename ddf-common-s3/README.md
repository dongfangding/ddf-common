# ddf-common-s3

> S3-compatible object storage universal module. Built on MinIO SDK with unified encapsulation, supports MinIO, AWS S3,
> Alibaba Cloud OSS, Tencent Cloud COS, and other compatible implementations. Provides file upload, presigned URLs,
> thumbnail generation, and automatic Bucket management.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-s3` solves the **"business systems need unified access to multiple object storage services"** problem.

| Scenario | Typical Problem | What the Module Provides |
| --- | --- | --- |
| Local development testing | Don't want to depend on external cloud storage | Local MinIO deployment, config-and-connect |
| Production cloud vendor switching | Different vendor SDKs have large differences; migration cost is high | Unified `S3Api` interface; switching only requires config changes |
| Image upload and preview | Need thumbnails and presigned access | `FileUploadHelper` auto-generates thumbnails; `S3Api` generates temporary access URLs |
| Temporary file sharing | Private Bucket files need time-limited access | Presigned download links that expire automatically |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-s3</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. Minimum Configuration

### MinIO Local Environment

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

### AWS S3 Production Environment

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

### Alibaba Cloud OSS

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

## 4. Core API

### 4.1 File Upload

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

### 4.2 Get Presigned Download URL

```java
public String getDownloadUrl(String objectKey) {
    // Link expires in 1 hour
    PresignedUrlResult result = s3Api.getPresignedDownloadUrl(objectKey, Duration.ofHours(1));
    return result.getUrl();
}
```

### 4.3 Image Upload with Thumbnail

```java
@Autowired
private FileUploadHelper fileUploadHelper;

public UploadResult uploadWithThumbnail(MultipartFile image) throws IOException {
    // Auto-validates image type, generates thumbnail, uploads both original and thumbnail
    return fileUploadHelper.uploadImage(image, "products/");
}
```

### 4.4 Delete Object

```java
s3Api.delete(objectKey);
```

---

## 5. Advanced Usage / Extension Points

### 5.1 Switching Storage Services

Only configuration changes are needed to switch compatible implementations; zero business code changes:

| Provider | endpoint example | path-style-access |
| --- | --- | --- |
| MinIO | `http://localhost:9000` | `true` |
| AWS S3 | `https://s3.amazonaws.com` | `false` |
| Alibaba Cloud OSS | `https://oss-cn-hangzhou.aliyuncs.com` | `false` |
| Tencent Cloud COS | `https://cos.ap-guangzhou.myqcloud.com` | `false` |

### 5.2 Automatic Bucket Creation

The module checks whether the Bucket exists before upload and auto-creates it if missing. To disable:

```yaml
customizer:
  infra:
    s3:
      auto-create-bucket: false
```

### 5.3 File Type Whitelist

`FileUploadHelper` only allows image types by default. To support documents, videos, etc.:

```yaml
customizer:
  infra:
    s3:
      allowed-file-types: jpg,jpeg,png,gif,pdf,doc,docx,mp4
```

### 5.4 Temporary Credentials (Session Token)

When `session-token` is configured, the module signs requests using temporary credentials, suitable for STS-authorized scenarios:

```yaml
customizer:
  infra:
    s3:
      access-key: TEMP_ACCESS_KEY
      secret-key: TEMP_SECRET_KEY
      session-token: TEMP_SESSION_TOKEN
```

---

## 6. Interplay with Other Modules

| Module | How They Cooperate |
| --- | --- |
| `ddf-common-vps` | VPS module provides FastDFS file storage; S3 module provides cloud object storage. Choose one by scenario or combine them |
| `ddf-common-core` | JSON serialization and utility support |
| `ddf-common-api` | Response DTO definitions such as `UploadResult`, `PresignedUrlResult` |

---

## 7. FAQ

**Q1: Why is the configuration prefix `customizer.infra.s3` instead of `ddf.s3`?**
The project uniformly uses `customizer.infra.<feature>` as the infrastructure configuration prefix, consistent with starter auto-configuration conventions.

**Q2: What does `path-style-access` do?**
When `true`, uses path-style access (`http://endpoint/bucket/object`); when `false`, uses virtual-hosted style (`http://bucket.endpoint/object`). MinIO typically needs `true`; AWS S3 needs `false`.

**Q3: What's the maximum validity period for presigned URLs?**
Depends on the specific storage service limit; generally recommended not to exceed 7 days. For longer periods, consider public Buckets or CDN integration.

**Q4: Is multipart upload for large files supported?**
The current module encapsulates standard single-file upload interfaces. For multipart upload, use MinIO SDK's `composeObject` or AWS S3's Multipart Upload API directly.

---

## 8. References

- Source: `S3Api`, `S3Service`, `S3Helper`, `FileUploadHelper`, `S3Properties`
- MinIO Java SDK: https://min.io/docs/minio/linux/developers/java/minio-java.html
