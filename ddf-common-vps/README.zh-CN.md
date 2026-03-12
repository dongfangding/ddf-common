# ddf-common-vps

[English](./README.md) | [中文](./README.zh-CN.md)

文件与图片处理支撑模块。

## 当前定位

- 提供基于 FastDFS 的文件处理接入
- 提供图片与视频相关辅助能力
- 通过自动配置暴露 `VpsProperties` 与相关工具组件

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-vps</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 自动配置

- `com.ddf.common.vps.config.VpsAutoConfiguration`

## 主要类型

- `VpsProperties`
- `VpsClient`
- `VpsUtil`

## 配置前缀

```yaml
customizer:
  infra:
    vps:
      ffmpegTmpPath: /opt/ffmpeg/tmp/
      fdfsBasePath: /data/fastdfs
```

## 说明

- 当前模块对 FastDFS 和本地处理链路存在一定场景约束
- 接入前建议先确认与你的文件存储方案是否匹配
