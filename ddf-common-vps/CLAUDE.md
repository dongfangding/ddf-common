# CLAUDE.md

## 模块简介

VPS 工具模块，提供文件上传和图片处理功能。

## 核心类

| 类路径                                                | 功能         |
|----------------------------------------------------|------------|
| `com.ddf.boot.common.vps.config.FastDfsProperties` | FastDFS 配置 |
| `com.ddf.boot.common.vps.api.VpsApi`               | 文件操作接口     |
| `com.ddf.boot.common.vps.util.ImageUtil`           | 图片处理工具     |

## 使用说明

### 配置

```yaml
ddf:
  vps:
    fastdfs:
      tracker-server: tracker-server:22122
```

### 上传文件

```java
@Autowired
private VpsApi vpsApi;

public String uploadFile(MultipartFile file) {
    return vpsApi.upload(file);
}
```

### 图片压缩

```java
@Autowired
private ImageUtil imageUtil;

public void compressImage(String sourcePath, String targetPath) {
    imageUtil.compress(sourcePath, targetPath, 0.8f);
}
```

## 注意事项

1. **存储服务**：确保 FastDFS 服务可用
2. **图片大小**：注意图片压缩质量和尺寸限制
