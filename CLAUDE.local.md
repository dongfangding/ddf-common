# CLAUDE.md

## 构建命令

**前置条件**：`game` 是 `seaway` 多仓库项目的子模块，其 parent POM 为 `com.kewta.common:deck`。
`game/pom.xml` 中 `<relativePath/>` 为空，不会向上查找本地目录，因此**必须先将依赖的 parent POM 安装到本地 Maven 仓库**，否则直接在 `game/` 下执行 `mvn` 会报 `Non-resolvable parent POM`。

**Settings 文件**：本项目使用非默认的 Maven settings，所有 `mvn` 命令都必须指定：

```bash
-s /mnt/d/develop_tools/apache-maven-3.9.9/conf/settings-snowball-wsl.xml
```

依赖安装顺序（均在 seaway 根目录下执行，注意加上 `-s`）：

```bash
# 1. 安装 wheel（deck 的 BOM 依赖）
cd ../wheel && mvn -s /mnt/d/develop_tools/apache-maven-3.9.9/conf/settings-snowball-wsl.xml clean install -DskipTests

# 2. 安装 deck（game 的 parent POM）
cd ../deck && mvn -s /mnt/d/develop_tools/apache-maven-3.9.9/conf/settings-snowball-wsl.xml clean install -DskipTests

# 3. 之后即可在 game 目录下正常构建
cd ../game && mvn -s /mnt/d/develop_tools/apache-maven-3.9.9/conf/settings-snowball-wsl.xml clean package -DskipTests
```

常用命令：

```bash
# 本地构建（跳过测试）
mvn -s /mnt/d/develop_tools/apache-maven-3.9.9/conf/settings-snowball-wsl.xml clean package -DskipTests

# 完整构建（含测试）
mvn -s /mnt/d/develop_tools/apache-maven-3.9.9/conf/settings-snowball-wsl.xml clean package -U

# 安装到本地仓库
mvn -s /mnt/d/develop_tools/apache-maven-3.9.9/conf/settings-snowball-wsl.xml clean install

# 运行单元测试
mvn -s /mnt/d/develop_tools/apache-maven-3.9.9/conf/settings-snowball-wsl.xml test

# 运行单个测试类 / 方法
mvn -s /mnt/d/develop_tools/apache-maven-3.9.9/conf/settings-snowball-wsl.xml test -Dtest=PlinkoServiceTest
mvn -s /mnt/d/develop_tools/apache-maven-3.9.9/conf/settings-snowball-wsl.xml test -Dtest=PlinkoServiceTest#testPlay

```
