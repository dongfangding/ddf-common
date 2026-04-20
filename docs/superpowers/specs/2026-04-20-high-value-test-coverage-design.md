# ddf-common 基础链路高价值补测设计

## 1. 背景与目标

本次工作面向 `D:\IdeaWorkspaces\ddf-common` 全量仓库，而不是仅针对最近提交。目标不是一次性补齐所有模块的测试空白，而是优先为全仓库中最核心、复用度最高、回归代价最高的公共能力建立测试保护层。

本轮设计聚焦以下目标：

1. 为基础公共链路上的核心模块补充高价值测试，而不是追求模块覆盖面最大化。
2. 同时覆盖两类测试：
   - 行为测试：约束工具类、Helper、上下文工具、辅助组件的核心输入输出契约。
   - 装配测试：约束自动装配、条件装配、默认 Bean 注册与关键接入路径。
3. 保证新增测试稳定、可重复执行，不依赖外部 Redis、MQ、网络或其他不可控环境。
4. 为后续继续扩展到其他模块提供统一的筛选标准、命名规范和执行策略。

## 2. 范围定义

### 2.1 本轮纳入模块

本轮纳入以下 6 个模块，形成一条“基础链路闭环”：

1. `ddf-common-api`
2. `ddf-common-core`
3. `ddf-common-mvc`
4. `ddf-common-authentication`
5. `ddf-common-redis`
6. `ddf-common-limit`

### 2.2 选择原因

#### `ddf-common-api`

该模块承载公共 DTO、异常模型、上下文工具、日期/JSON/数字等高复用能力，一旦回归，会影响多个上层模块。

#### `ddf-common-core`

该模块是仓库中的公共基础能力中心，聚合了字符串、安全、缓存、线程、随机、对象处理等大量核心工具，是本轮行为测试覆盖的主战场。

#### `ddf-common-mvc`

该模块承载 Web 基础链路能力，既包含自动装配入口，也包含统一消息、Web、AOP 相关帮助工具，适合同时做行为测试和装配测试。

#### `ddf-common-authentication`

该模块负责认证基础接入能力，测试重点应放在自动装配契约、默认行为和关键认证辅助能力的稳定性上。

#### `ddf-common-redis`

该模块是基础设施支撑模块，被多个能力复用，既需要验证自动装配入口，也需要验证 Redis 辅助工具的关键行为。

#### `ddf-common-limit`

该模块承担限流与防重复提交能力，一旦逻辑或装配回归，影响直接且隐蔽，具有较高测试收益。

### 2.3 本轮明确不纳入

以下模块暂不纳入本轮主范围：

- `ddf-common-canal`
- `ddf-common-captcha`
- `ddf-common-distributed-lock`
- `ddf-common-script`
- `ddf-common-sharding`
- `ddf-common-xxl-executor`

原因不是这些模块不重要，而是为了确保本轮聚焦基础链路、做深做稳，避免范围失控。

## 3. 测试分层设计

### 3.1 行为测试

行为测试面向以下对象：

- 工具类
- Helper 类
- 上下文工具
- 转换器
- 核心辅助组件

行为测试的目标：

1. 验证公开输入输出契约。
2. 覆盖空值、边界值、默认值、异常分支等容易回归的路径。
3. 尽量面向公开行为断言，不锁死过多内部实现细节。

### 3.2 装配测试

装配测试面向以下对象：

- `@AutoConfiguration`
- 条件装配
- 默认 Bean 注册
- 依赖存在/缺失时的关键装配路径

装配测试的目标：

1. 验证模块最小接入能力。
2. 验证关键 Bean 在满足条件时能正确注册。
3. 验证关键条件分支不会因后续改动而失效。

## 4. 各模块测试目标

### 4.1 `ddf-common-api`

本轮以行为测试为主，优先覆盖：

- 公共响应模型、分页模型、异常模型的构造与边界行为。
- `JsonUtil`
- `DateUtils`
- `PatternUtil`
- `NumberUtil`
- `UserContextUtil`

装配侧保留现有 `ApiAutoConfiguration` 结构保护，不在本轮继续扩张。

### 4.2 `ddf-common-core`

本轮为该模块做最深覆盖，以行为测试为主，必要时补少量结构保护。优先覆盖：

- `StringExtUtil`
- `SecureUtil`
- `LocalCacheUtil`
- `ThreadBuilderHelper`
- 其他高复用、逻辑分支明显的核心工具类

对于已有测试的类，如果当前测试已能保护核心契约，则优先补明显缺口，而不是机械重复。

### 4.3 `ddf-common-mvc`

本轮同时做行为测试和装配测试，重点覆盖：

- `MvcAutoConfiguration`
- `MessageSourceUtil`
- `WebUtil`
- `AopUtil`

目标是验证基础 MVC 能力、工具类契约与自动装配入口是否稳定，不做重型 Web 集成测试。

### 4.4 `ddf-common-authentication`

本轮同时做行为测试和装配测试，重点覆盖：

- `AuthenticationAutoConfiguration`
- 最核心的认证辅助组件或默认组件行为
- 空配置、缺失依赖、默认启用/禁用等关键条件路径

### 4.5 `ddf-common-redis`

本轮同时做行为测试和装配测试，重点覆盖：

- `RedisCustomizeAutoConfiguration`
- `RedisCommandHelper`
- `RedisTemplateHelper`
- 如测试成本合适，再覆盖 `GeoHelper`

测试策略优先选择纯单测或 Mock 测试，不依赖真实 Redis 实例。

### 4.6 `ddf-common-limit`

本轮同时做行为测试和装配测试，重点覆盖：

- `LimitAutoConfiguration`
- 限流 key 生成
- 注解解析
- 默认策略
- 重复提交判定相关核心类

本轮不做复杂并发压测，重点保护规则解释和装配行为。

## 5. 挑选标准

本轮测试对象按照以下优先级筛选：

1. 跨模块复用高。
2. 逻辑分支明显。
3. 回归代价高。
4. 可通过 JUnit、Mockito、`ApplicationContextRunner` 稳定测试。

以下对象原则上暂缓：

- 几乎无逻辑分支的简单常量类、枚举类、纯 DTO/VO。
- 强依赖外部环境、难以稳定执行的类。
- 低复用、低风险、局部使用的简单工具类。
- 已有测试已经能够有效保护核心契约的类。

## 6. 命名与组织规范

为保持仓库一致性，新增测试建议遵循以下命名规则：

### 6.1 行为测试

文件名使用 `XxxTest`，例如：

- `JsonUtilTest`
- `MessageSourceUtilTest`
- `RedisCommandHelperTest`

### 6.2 自动装配测试

文件名使用 `XxxAutoConfigurationTest` 或 `XxxStructureTest`，例如：

- `MvcAutoConfigurationTest`
- `AuthenticationAutoConfigurationTest`
- `LimitAutoConfigurationStructureTest`

### 6.3 注入风格测试

如模块已有统一约束，继续沿用 `InjectionStyleTest` 风格，但仅在确有统一价值时新增，不机械复制。

## 7. 执行策略

### 7.1 实施顺序

建议按以下顺序分批实施：

1. `ddf-common-api` + `ddf-common-core`
2. `ddf-common-mvc` + `ddf-common-authentication`
3. `ddf-common-redis` + `ddf-common-limit`

原因：

- 前两者是公共基础能力，测试独立性更强，适合作为第一批稳定基线。
- 中间两者是 Web 基础链路，适合在基础能力稳定后补装配与行为保护。
- 后两者虽价值高，但在依赖与测试设计上略复杂，适合放到第三批实施。

### 7.2 环境前提

- 当前终端环境：Windows PowerShell
- 当前仓库路径：`D:\IdeaWorkspaces\ddf-common`
- Java 版本：17
- Maven 版本要求：3.9.6 及以上

执行 Maven 时需要明确：

1. 工作目录为仓库根目录 `D:\IdeaWorkspaces\ddf-common`
2. 按模块执行时使用 `-pl` 指定目标模块
3. 如需指定 `settings.xml`，必须在执行前显式给出完整路径，不默认假设

### 7.3 验证策略

优先按模块定向执行新增测试，不在一开始就做全仓库回归。验证时优先运行新增测试所属模块，确保问题可快速定位。

## 8. 实施边界

### 8.1 允许的代码变更

本轮只做以下两类变更：

1. 测试代码新增与完善
2. 为测试服务的最小可测试性调整

如果个别类难以测试，允许做很小的可测试性改造，例如：

- 提取局部逻辑
- 降低对静态状态的直接依赖
- 暴露 package-private 入口供测试使用

但不得借题发挥做大规模重构。

### 8.2 明确不做

- 不做全仓库覆盖率指标冲刺
- 不引入 Testcontainers 等重型测试基础设施
- 不做外部服务依赖型集成测试
- 不顺手修改与本次测试目标无关的问题

## 9. 风险与控制

### 9.1 历史代码可测试性差

部分历史工具类可能依赖静态上下文、系统时间、线程或环境变量。处理原则是优先测试稳定部分，确有必要时仅做最小可测试性调整。

### 9.2 装配测试脆弱

某些自动装配类若使用完整 Spring 容器，测试会变重且脆弱。本轮统一优先使用 `ApplicationContextRunner` 构建最小上下文。

### 9.3 测试与实现耦合过深

行为测试尽量围绕公开输入输出和外部契约，装配测试关注 Bean 注册与条件生效，不锁死过多内部实现细节。

## 10. 验收口径

本轮完成标准以“关键回归保护”而不是“测试文件数量”为准，验收口径如下：

1. 入选模块均新增高价值测试，而不是重复已有测试思路。
2. `ddf-common-api` 与 `ddf-common-core` 形成一组稳定的公共契约保护测试。
3. `ddf-common-mvc`、`ddf-common-authentication`、`ddf-common-redis`、`ddf-common-limit` 形成关键装配路径保护，其中至少部分模块同时补到核心行为测试。
4. 新增测试可以独立执行且结果稳定，不依赖外部不可控环境。

## 11. 后续计划入口

本设计确认后，下一步进入实现计划编写阶段。实现计划需要进一步拆分：

1. 每个模块的目标类与测试文件清单
2. 每批次的实施顺序与依赖关系
3. 每批次的验证命令与通过标准
4. 对必要的最小可测试性调整进行单独标注
