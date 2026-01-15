# ddf-common

Spring Boot 3.3 多模块通用组件库，为 Java 后端开发提供可复用组件。

## 特性

- **开箱即用**: 引入依赖即可完成功能集成，无需繁琐配置
- **模块化设计**: 按功能拆分模块，按需引入
- **生产可用**: 代码贴近生产项目开发标准，可直接用于生产环境

## 技术栈

| 技术 | 版本 |
|------|------|
| Java | 17 |
| Spring Boot | 3.3.13 |
| Maven | 3.9.6+ |

## 模块列表

| 模块 | 功能 |
|------|------|
| [ddf-common-dependency](./ddf-common-dependency/README.md) | 依赖版本统一管理 (BOM) |
| [ddf-common-api](./ddf-common-api/README.md) | 通用 API 定义和枚举 |
| [ddf-common-core](./ddf-common-core/README.md) | 核心功能（基础配置、工具类） |
| [ddf-common-mvc](./ddf-common-mvc/README.md) | MVC 相关工具 |
| [ddf-common-authentication](./ddf-common-authentication/README.md) | JWT 认证授权 |
| [ddf-common-redis](./ddf-common-redis/README.md) | Redis 集成 (Redisson) |
| [ddf-common-distributed-lock](./ddf-common-distributed-lock/README.md) | 分布式锁 (Redis/ZK) |
| [ddf-common-limit](./ddf-common-limit/README.md) | 限流和防重复提交 |
| [ddf-common-captcha](./ddf-common-captcha/README.md) | 验证码 (Google kaptcha + 安吉) |
| [ddf-common-ids-service](./ddf-common-ids-service/README.md) | ID 生成 (雪花算法/号段模式) |
| [ddf-common-rocketmq](./ddf-common-rocketmq/README.md) | RocketMQ 集成 |
| [ddf-common-ons](./ddf-common-ons/README.md) | 阿里云 ONS |
| [ddf-common-mqtt](./ddf-common-mqtt/README.md) | MQTT 协议支持 |
| [ddf-common-mqtt-client](./ddf-common-mqtt-client/README.md) | MQTT 客户端 |
| [ddf-common-canal](./ddf-common-canal/README.md) | Canal 数据库同步 |
| [ddf-common-mongo](./ddf-common-mongo/README.md) | MongoDB 集成 |
| [ddf-common-es](./ddf-common-es/README.md) | Elasticsearch 集成 |
| [ddf-common-zookeeper](./ddf-common-zookeeper/README.md) | Zookeeper 服务发现 |
| [ddf-common-websocket](./ddf-common-websocket/README.md) | WebSocket 支持 |
| [ddf-common-netty-broker](./ddf-common-netty-broker/README.md) | Netty 自定义协议实现 |
| [ddf-common-xxl-executor](./ddf-common-xxl-executor/README.md) | XXL-JOB 执行器 |
| [ddf-common-log4j](./ddf-common-log4j/README.md) | Log4j2 配置 |
| [ddf-common-sharding](./ddf-common-sharding/README.md) | ShardingSphere 集成 |
| [ddf-common-alarm](./ddf-common-alarm/README.md) | 告警通知模块 |
| [ddf-common-third-party](./ddf-common-third-party/README.md) | 第三方集成 (OSS, SMS) |
| [ddf-common-vps](./ddf-common-vps/README.md) | VPS 工具（文件上传） |
| [ddf-common-script](./ddf-common-script/README.md) | 工具脚本 |

## 快速开始

### 添加依赖

```xml
<dependency>
    <groupId>com.ddf.common</groupId>
    <artifactId>ddf-common-core</artifactId>
    <version>boot3.3-2025.1-SNAPSHOT</version>
</dependency>
```

### 构建命令

```bash
# 构建所有模块
mvn clean install -DskipTests

# 构建指定模块
mvn clean install -DskipTests -pl ddf-common-core -am
```

## 配合使用

本项目为通用依赖库，不可直接运行。配套示例项目：[spring-boot-quick](https://github.com/dongfangding/spring-boot-quick)

## License

MIT
