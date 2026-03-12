# 发布到 Maven Central

[English](./releasing-to-maven-central.md) | [中文](./releasing-to-maven-central.zh-CN.md)

本项目已按 Sonatype Central Publisher Portal 的发布方式完成准备。

## 前置条件

- Java 17
- Maven 3.9.6 或更高版本
- 拥有 `io.github.dongfangding` 命名空间发布权限的 Sonatype Central 账号
- 已安装并配置 GPG，用于产物签名
- `settings.xml` 中已配置根 `pom.xml` 所引用的发布服务器凭据

## 本地校验

发布前先执行一次编译检查：

```bash
mvn -q -DskipTests compile
```

如果需要更完整的校验，可以执行：

```bash
mvn clean verify
```

## 发布构建

使用 `release` profile 以附带源码包、Javadocs、签名，并调用中央仓库发布插件：

```bash
mvn -Prelease clean deploy
```

当前首个公开发布候选版本：

- `boot3.5-2026.1`

## GitHub Actions Secrets

如果使用仓库中的 `.github/workflows/release-central.yml`，需要配置以下仓库 Secrets：

- `MAVEN_USERNAME`
- `MAVEN_PASSWORD`
- `MAVEN_GPG_PRIVATE_KEY`
- `MAVEN_GPG_PASSPHRASE`

## 说明

- 根 POM 已声明 Maven Central 所需的项目元数据。
- 子模块应保持清晰的 `name` 和 `description`，因为这些信息会展示在仓库索引中。
- `ddf-common-script` 更适合作为内部工具模块，而不是公开运行时依赖。
- `ddf-common-netty-broker` 当前按示例或专项场景模块处理，不纳入默认 Central 发布集合。
- 如果发布凭据或 GPG 配置有变化，应更新本地 `settings.xml`，而不是将敏感信息提交到仓库。
