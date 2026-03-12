# 公开模块策略

[English](./public-module-policy.md) | [中文](./public-module-policy.zh-CN.md)

本文件定义哪些模块默认应纳入公开 Maven Central 发布集合。

## 默认公开

以下类型的模块默认适合公开发布：

- 共享 API 与核心支撑模块
- Spring Boot starter 模块
- 坐标稳定的基础设施集成模块
- 可复用的中间件集成模块

## 默认排除

以下模块当前默认不纳入 Maven Central 发布集合：

- `ddf-common-script`
- `ddf-common-netty-broker`

## 原因说明

### `ddf-common-script`

- 主要包含内部脚本与离线工具资源
- 不属于典型的运行时依赖
- 更适合作为仓库内部工具而不是公开库产物

### `ddf-common-netty-broker`

- 相比已发布核心模块，更偏向示例和专项场景
- 在作为稳定公开产物之前，还需要更清晰的 API 边界定义

## 重新纳入条件

被排除的模块满足以下条件后，可以重新纳入公开发布集合：

- API 边界经过有意识设计并完成文档化
- 具有明确的公开使用场景
- 至少具备基础自动化验证
- README 反映的是受支持的使用方式，而不是内部试验内容
