# ddf-common-vps

> File and image processing support module. Provides file upload based on FastDFS, plus image compression,
> video processing, and other media-handling utilities.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-vps` solves the **"file storage and media processing"** problem.

| Scenario                        | Typical Problem                                      | What the Module Provides             |
|---------------------------------|------------------------------------------------------|--------------------------------------|
| User avatar / image upload      | Files stored on local disk cannot scale horizontally | FastDFS distributed file storage     |
| Image compression / thumbnails  | Original images are too large and load slowly        | `ImageUtil` image compression        |
| Video transcoding / screenshots | Need FFmpeg video processing                         | Video processing helper tools        |
| File management                 | Need delete and query after upload                   | `VpsApi` unified operation interface |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-vps</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. Minimum Configuration

```yaml
ddf:
  vps:
    fastdfs:
      tracker-server: tracker-server:22122
    ffmpeg-tmp-path: /opt/ffmpeg/tmp/
    fdfs-base-path: /data/fastdfs
```

---

## 4. Core API

### 4.1 File upload

```java
@Autowired
private VpsApi vpsApi;

public String uploadFile(MultipartFile file) {
    return vpsApi.upload(file);
}
```

### 4.2 Image compression

```java
@Autowired
private ImageUtil imageUtil;

public void compressImage(String sourcePath, String targetPath) {
    // quality: 0.0 ~ 1.0
    imageUtil.compress(sourcePath, targetPath, 0.8f);
}
```

### 4.3 Get file access URL

```java
public String getFileUrl(String fileId) {
    return vpsApi.getUrl(fileId);
}
```

---

## 5. Advanced Usage / Extension Points

### 5.1 Batch upload

```java
List<String> fileIds = files.stream()
    .map(vpsApi::upload)
    .collect(Collectors.toList());
```

### 5.2 Image dimension limits

Validate image dimensions before upload:

```java
public void uploadAvatar(MultipartFile file) {
    BufferedImage image = ImageIO.read(file.getInputStream());
    if (image.getWidth() > 1024 || image.getHeight() > 1024) {
        throw new BusinessException("Image dimensions exceed limit");
    }
    vpsApi.upload(file);
}
```

---

## 6. Interplay with Other Modules

| Module                   | How They Cooperate                                                                |
|--------------------------|-----------------------------------------------------------------------------------|
| `ddf-common-third-party` | If you prefer Alibaba Cloud OSS over FastDFS, use the `OssApi` from `third-party` |
| `ddf-common-core`        | IO utilities, string processing, and other fundamentals                           |

---

## 7. FAQ

**Q1: Should I choose FastDFS or OSS?**

- Already have a FastDFS cluster and data volume is manageable → FastDFS (this module)
- Need CDN acceleration, cross-region replication, serverless → Alibaba Cloud OSS (`ddf-common-third-party`)

**Q2: Does image compression affect the original image?**
No. `ImageUtil.compress` generates a new file; the original is preserved.

**Q3: Is FFmpeg required for video processing?**
Yes. FFmpeg must be installed on the server, and `ffmpeg-tmp-path` must have read/write permissions.

---

## 8. References

- Source: `VpsApi`, `VpsClient`, `VpsProperties`, `ImageUtil`
- FastDFS docs: https://github.com/happyfish100/fastdfs
