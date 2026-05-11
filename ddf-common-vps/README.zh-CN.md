# ddf-common-vps

> 文件与图片处理支持模块。基于 FastDFS 提供文件上传能力，并提供图片压缩、视频处理等辅助功能。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-vps` 解决的是 **"文件存储与媒体处理"** 问题。

| 场景 | 典型问题 | 模块提供的能力 |
| --- | --- | --- |
| 用户头像/图片上传 | 文件存在本地磁盘，无法横向扩展 | FastDFS 分布式文件存储 |
| 图片压缩/缩略图 | 原图过大，加载慢 | `ImageUtil` 图片压缩 |
| 视频转码/截图 | 需要 FFmpeg 处理视频 | 视频处理辅助工具 |
| 文件管理 | 上传后需要删除、查询 | `VpsApi` 统一操作接口 |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-vps</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. 最小化配置

```yaml
ddf:
  vps:
    fastdfs:
      tracker-server: tracker-server:22122
    ffmpeg-tmp-path: /opt/ffmpeg/tmp/
    fdfs-base-path: /data/fastdfs
```

---

## 4. 核心 API

### 4.1 文件上传

```java
@Autowired
private VpsApi vpsApi;

public String uploadFile(MultipartFile file) {
    return vpsApi.upload(file);
}
```

### 4.2 图片压缩

```java
@Autowired
private ImageUtil imageUtil;

public void compressImage(String sourcePath, String targetPath) {
    // quality: 0.0 ~ 1.0
    imageUtil.compress(sourcePath, targetPath, 0.8f);
}
```

### 4.3 获取文件访问地址

```java
public String getFileUrl(String fileId) {
    return vpsApi.getUrl(fileId);
}
```

---

## 5. 进阶用法 / 扩展点

### 5.1 批量上传

```java
List<String> fileIds = files.stream()
    .map(vpsApi::upload)
    .collect(Collectors.toList());
```

### 5.2 图片尺寸限制

在上传前校验图片尺寸：

```java
public void uploadAvatar(MultipartFile file) {
    BufferedImage image = ImageIO.read(file.getInputStream());
    if (image.getWidth() > 1024 || image.getHeight() > 1024) {
        throw new BusinessException("图片尺寸超过限制");
    }
    vpsApi.upload(file);
}
```

---

## 6. 与其他模块协作

| 模块 | 协作方式 |
| --- | --- |
| `ddf-common-third-party` | 如需使用阿里云 OSS 替代 FastDFS，可引入 `third-party` 模块的 `OssApi` |
| `ddf-common-core` | IO 工具、字符串处理等基础支撑 |

---

## 7. FAQ

**Q1：FastDFS 与 OSS 怎么选？**
- 已有 FastDFS 集群且数据量可控 → FastDFS（本模块）
- 需要 CDN 加速、跨区域复制、Serverless → 阿里云 OSS（`ddf-common-third-party`）

**Q2：图片压缩会影响原图吗？**
不会。`ImageUtil.compress` 生成新文件，原图保留。

**Q3：视频处理需要额外安装 FFmpeg 吗？**
是。需在服务器安装 FFmpeg，并确保 `ffmpeg-tmp-path` 有读写权限。

---

## 8. 参考

- 源码：`VpsApi`、`VpsClient`、`VpsProperties`、`ImageUtil`
- FastDFS 文档：https://github.com/happyfish100/fastdfs
