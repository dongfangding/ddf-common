# ddf-common-vps

[English](./README.md) | [中文](./README.zh-CN.md)

File and image processing support module.

## Current Positioning

- Provides file processing integration based on FastDFS
- Provides helper support for image and video-related handling
- Exposes `VpsProperties` and related utility components through auto-configuration

## Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-vps</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## Auto-configuration

- `com.ddf.common.vps.config.VpsAutoConfiguration`

## Main Types

- `VpsProperties`
- `VpsClient`
- `VpsUtil`

## Configuration Prefix

```yaml
customizer:
  infra:
    vps:
      ffmpegTmpPath: /opt/ffmpeg/tmp/
      fdfsBasePath: /data/fastdfs
```

## Notes

- The current module has some scenario constraints around FastDFS and local processing flows
- Confirm compatibility with your own file storage strategy before adoption
