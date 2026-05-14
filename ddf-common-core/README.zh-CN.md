# ddf-common-core

> 通用内核模块：在 `ddf-common-api` 之上提供 Spring 上下文、线程池治理、雪花 ID、加解密 / 签名、
> 本地缓存、Bean 拷贝、树形结构、分页桥接等一组**与业务无关的纯工具能力**。
> 几乎所有 ddf-common 模块都直接或间接依赖它，但**业务工程一般无需显式引入**。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-core` 在四层架构中位于**基础内核层**，向上承担 Spring/JDK 层面的"通用基础设施"，
向下只依赖 `ddf-common-api`。它不提供数据库、Redis、MQ 等基础设施，但**为这些基础设施提供共用能力**。

| 场景                      | 典型问题                                         | 模块提供的能力                                                          |
|-------------------------|----------------------------------------------|------------------------------------------------------------------|
| 非 Bean 环境访问 Spring Bean | `static` 工具类里需要拿到 `RedisTemplate`            | `SpringContextHolder.getBeanWithStatic(...)` 静默降级                |
| 分布式雪花 ID                | 不同实例 ID 不可重复，需配置 workerId / dataCenterId     | `IdsUtil.getNextLongId()` + `customizer.infra.global-properties` |
| RSA / AES / HMAC        | 网关签名、链路加密、敏感字段落库加密                           | `SecureUtil` + 启动期注入的 `rsa-*`/`aes-secret`/`sign-secret`         |
| 接口签名                    | 三方对接需要 ASCII 排序、嵌套对象扁平化                      | `SignatureUtil` 围绕 `BaseSign` 实现                                 |
| 线程池治理                   | 优雅停机、连接池泄漏排查、统一监控指标                          | `ThreadBuilderHelper.buildThreadPoolTaskExecutor` 自动登记           |
| 树形结构组装                  | 部门树、菜单树、地区树                                  | `TreeConvertUtil.convert(...)`                                   |
| 分页桥接                    | MyBatis PageHelper 与 Spring Data Pageable 互转 | `PageUtil`                                                       |
| Bean 拷贝                 | DTO ↔ Entity 高频转换，反射开销大                      | `BeanCopierUtils`（CGLIB BeanCopier + ReflectASM 构造缓存）            |
| Promise / 异步等待          | "下单后等回调，超时即失败" 的同步等待场景                       | `DeferredHelper` / `CompletableFutureHelper`                     |
| 环境识别                    | 区分 prod/dev/test，自动跳过某些初始化                   | `EnvironmentHelper`                                              |
| 本地缓存                    | 频繁但低基数读、不需要分布式一致性                            | `LocalCacheUtil`（Caffeine + Guava + Hutool TimedCache）           |

> ⚠️ 当前阶段 core 已经把 JDBC / Druid / Mail / Actuator 等"基础设施实现"迁出到独立 starter。
> 如果你需要这些能力，请改用 `ddf-common-data-mysql-starter` / `ddf-common-governance-starter`。

---

## 2. 依赖引入

绝大多数情况下 **你不需要直接依赖 core**：上层 starter（`ddf-common-starter-web` / `ddf-common-starter-default`）
已经传递引入。仅当你只想拿到工具类（如 `IdsUtil` / `SecureUtil`）时才显式声明：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-core</artifactId>
    <version>${ddf-common.version}</version>
    <!-- 推荐 optional，避免把 Spring / Hutool / Caffeine 强制传递给下游 -->
    <optional>true</optional>
</dependency>
```

主要传递依赖：

- `ddf-common-api`（协议根，提供 `BaseSign` / `ITreeTagCollection` / `ResponseData` 等）
- `spring-cloud-context`（提供 `@RefreshScope`，让 `GlobalProperties` 可被 SCG / Nacos 热刷新）
- `spring-boot-starter-cache`、`caffeine`、`guava`（本地缓存）
- `hutool-core` / `crypto` / `cache` / `extra` / `http` / `json`（工具集，**不要**和 `hutool-all` 混用）
- `aspectjweaver`、`httpclient5`、`jdeferred-core`、`reflectasm`

---

## 3. 最小化配置

模块的所有可调项集中在 `GlobalProperties`，前缀 **`customizer.infra.global-properties`**，全部为可选项：

```yaml
customizer:
  infra:
    global-properties:
      # 雪花 ID（必须确保多实例之间 workerId 不重复）
      snowflake-worker-id: 1
      snowflake-data-center-id: 1

      # RSA 密钥对（PEM 文本，使用 SecureUtil 时必须配置，否则抛 IllegalStateException）
      rsa-private-key: |
        -----BEGIN PRIVATE KEY-----
        ...
        -----END PRIVATE KEY-----
      rsa-public-key: |
        -----BEGIN PUBLIC KEY-----
        ...
        -----END PUBLIC KEY-----

      # AES / HMAC 密钥（使用 SecureUtil#aesEncrypt / SignatureUtil 时必须配置）
      aes-secret: ${AES_SECRET:}
      sign-secret: ${SIGN_SECRET:}

      # 全局异常 → HTTP 状态码：true 时 BusinessException 会把自身 code 写回 response.status
      exception-code-to-response-status: false

      # 详细日志开关 + 异常黑名单
      global-log-print-details: false
      ignore-log-exception-class-name:
        - org.springframework.web.servlet.NoHandlerFoundException
```

> 因为标注了 `@RefreshScope`，配置中心（Nacos / Apollo / SCG）发布 `actuator/refresh` 后会即时生效，
> 但需要谨慎处理"刷新瞬间还在执行的旧逻辑"。

### 3.1 自动注入的 Bean

`CoreAutoConfiguration` 启动后默认得到：

| Bean                                   | 类型                                          | 说明                                  |
|----------------------------------------|---------------------------------------------|-------------------------------------|
| `globalProperties`                     | `GlobalProperties`                          | 全局可调项（`@RefreshScope`）              |
| `springContextHolder`                  | `SpringContextHolder`                       | 启用 Hutool `SpringUtil`，提供静态 Bean 查找 |
| `environmentHelper`                    | `EnvironmentHelper`                         | 当前应用名 / 端口 / profile 工具             |
| `threadPoolExecutorShutdownDefinition` | `ExecutorServiceGracefulShutdownDefinition` | 默认 120s 优雅停机                        |
| `deferredHelper`                       | `DeferredHelper<?, ?, ?>`                   | jdeferred 适配的"等待回调"工具               |
| `completableFutureHelper`              | `CompletableFutureHelper<?>`                | 围绕 `CompletableFuture` 的工具          |

所有 Bean 都带 `@ConditionalOnMissingBean`，业务可自行覆盖。

---

## 4. 核心 API 使用指南

### 4.1 SpringContextHolder：在非 Bean 环境访问容器

```java
// 标准用法：抛 NoSuchBeanDefinitionException 当 Bean 不存在
RedisTemplate<?, ?> redisTemplate = SpringContextHolder.getBean(RedisTemplate.class);

// 静默降级：Bean 不存在 / 容器未启动时返回 null，常用于纯单测 / 启动期工具调用
GlobalProperties props = SpringContextHolder.getBeanWithStatic(GlobalProperties.class);
if (props == null) { /* 走默认值或单测桩 */ }

// 类型 + 名称组合查找
PrincipalFactory factory = SpringContextHolder.getBean("customPrincipalFactory", PrincipalFactory.class);

// 整族 Bean
Map<String, RedisKeyConstraint> all = SpringContextHolder.getBeansOfType(RedisKeyConstraint.class);
```

> `SpringContextHolder` 通过 Hutool `@EnableSpringUtil` 间接生效，**只要 core 模块在依赖路径中即可**，
> 业务不需要手动声明。

### 4.2 IdsUtil：雪花 ID

```java
long orderId = IdsUtil.getNextLongId();        // 标准 64bit
String traceId = IdsUtil.getNextStrId();       // 字符串形态（适合日志 / Mongo）
```

底层使用 Hutool `IdUtil.getSnowflake(workerId, dataCenterId)`，
workerId / dataCenterId 从 `GlobalProperties` 读取。

> ⚠️ **多实例部署时 workerId 必须按实例区分**（例如通过 ConfigMap / 启动参数注入），
> 否则会出现极小概率的 ID 重复。生产环境强烈建议结合 ddf-common-ids-service 集中分发。

### 4.3 SecureUtil：RSA / AES

```java
// RSA：私钥签名 / 公钥验签（PKCS#8 PEM 已被加载到 GlobalProperties）
String signature = SecureUtil.rsaSign("payload-to-sign");
boolean ok = SecureUtil.rsaVerify("payload-to-sign", signature);

// 报文加解密
String cipher = SecureUtil.rsaEncryptByPublicKey("sensitive-data");
String plain  = SecureUtil.rsaDecryptByPrivateKey(cipher);

// 对称加密
String aesCipher = SecureUtil.aesEncrypt("plain-text");
String aesPlain  = SecureUtil.aesDecrypt(aesCipher);

// 密码强校验
String hash = SecureUtil.bcryptEncode("user-password");
boolean matches = SecureUtil.bcryptMatches("user-password", hash);
```

> 密钥未配置时调用相关方法会抛 `IllegalStateException`。建议把私钥放到密钥管理服务（KMS / Vault），
> 通过环境变量注入 `customizer.infra.global-properties.rsa-private-key`，不要直接写入 `application.yml`。

### 4.4 SignatureUtil：接口签名

```java
// 嵌套对象自动扁平化、字段按 ASCII 排序、固定盐拼接、HMAC-SHA256 输出
BaseSign sign = SignatureUtil.signature(requestDto);
httpHeaders.set("X-Signature", sign.getSign());
httpHeaders.set("X-Timestamp", String.valueOf(sign.getTimestamp()));

// 服务端验签
SignatureUtil.verify(requestDto, sign);   // 失败抛 BusinessException
```

`SignatureUtil` 在内部调用 `asciiSortToQueryStringOnlyBasicType` 把对象拍平成
`a=1&b.c=2&b.d=3` 形态，因此对嵌套 DTO 也能稳定签名。

### 4.5 ThreadBuilderHelper：受治理的线程池

```java
ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadPoolTaskExecutor(
        "order-callback",   // 线程名前缀，用于日志定位
        4,                  // core
        16,                 // max
        500,                // queueCapacity
        60                  // keepAliveSeconds
);
executor.execute(() -> handleCallback(payload));
```

构建出来的执行器具有三项约定：

1. **自动登记到优雅停机注册表**，应用关闭时按 `ExecutorServiceGracefulShutdownDefinition` 等待 120s
2. **加入静态 `POOLS` 列表**，框架内置定时器会定期打印 `active / queue / completed` 指标
3. **统一拒绝策略**：默认 `CallerRunsPolicy`，避免任务静默丢弃

### 4.6 BeanCopierUtils：高性能拷贝

```java
UserDTO dto = BeanCopierUtils.copyProperties(userEntity, UserDTO.class);
List<UserDTO> list = BeanCopierUtils.copyListProperties(entities, UserDTO::new);
```

工具基于 CGLIB `BeanCopier` 缓存，配合 ReflectASM `ConstructorAccess` 完成实例化，比纯反射快一个数量级。

> ⚠️ 与 Spring `BeanUtils.copyProperties` 相比，CGLIB **不会**自动处理 `int ↔ Integer` 等装箱差异。
> 字段类型必须严格一致，否则会被静默跳过。建议在拷贝前用 `BaseDomain` / Lombok 约束字段类型。

### 4.7 TreeConvertUtil：树形结构

```java
// 部门列表实现 ITreeTagCollection<Long, DeptDTO>
List<DeptDTO> flatList = deptMapper.listAll();
List<DeptDTO> tree = TreeConvertUtil.convert(flatList);

// 树结构 + 排序（依赖 ITreeTagCollection#getSort）
List<DeptDTO> sortedTree = TreeConvertUtil.convertWithSort(flatList);
```

要求 DTO 实现 `ITreeTagCollection<K, T>`：声明自身 ID、父 ID、子节点容器即可。

### 4.8 PageUtil：分页桥接

```java
// Service 层：Spring Data PageRequest → PageHelper
PageHelper.startPage(PageUtil.toPageNum(query), query.getPageSize());
List<UserVO> list = userMapper.list(query);

// 出参：PageInfo → 通用 PageResult
return PageUtil.buildPageResult(new PageInfo<>(list));
```

`PageResult<T>` 来自 `ddf-common-api`，可直接被前端按统一结构消费。

### 4.9 LocalCacheUtil：本地缓存

```java
// Caffeine + 自动过期
String region = LocalCacheUtil.computeIfAbsent("region:" + cityCode,
        Duration.ofMinutes(10),
        () -> cityClient.queryRegion(cityCode));

// 配合 Guava LoadingCache 适合"key 数量较少 + 调用极频繁"
LoadingCache<Long, UserBaseInfo> userCache = LocalCacheUtil.buildLoadingCache(
        Duration.ofMinutes(5), 5_000, userId -> userMapper.findById(userId));
```

> 本地缓存与 Redis **不一致**，跨实例分布式数据请走 `ddf-common-redis`。

### 4.10 异步等待：Deferred / CompletableFuture

```java
// 场景：发起支付后等待回调；如 30s 内无回调则按超时处理
String txId = paymentClient.pay(request);
PaymentCallback callback = deferredHelper.acquire(txId, 30_000);   // 阻塞等待

// 回调线程
deferredHelper.resolve(txId, callback);
```

`DeferredHelper` 与 `CompletableFutureHelper` 主要解决"业务 ID ↔ Future" 的字典管理与超时清理，
真正的回调来源（HTTP / MQ / Webhook）由业务自行触发。

### 4.11 BaseDomain：实体基类

```java
@SuperBuilder(toBuilder = true)
@Data @EqualsAndHashCode(callSuper = true)
public class UserDO extends BaseDomain {
    private String username;
    private String mobile;
}
```

`BaseDomain` 自带 `id`、`gmtCreated`、`gmtModified`（毫秒时间戳），
统一了所有持久化层实体的命名与审计字段。

---

## 5. 进阶用法 / 扩展点

### 5.1 自定义优雅停机时长

默认 120 秒，业务可在自己的 `@Configuration` 里重写 Bean：

```java
@Bean
public ExecutorServiceGracefulShutdownDefinition threadPoolExecutorShutdownDefinition() {
    return new ExecutorServiceGracefulShutdownDefinition(30, TimeUnit.SECONDS);
}
```

### 5.2 注册业务自定义线程池到优雅停机

不通过 `ThreadBuilderHelper` 创建的 `ExecutorService`，需要手动登记：

```java
@Bean
public ExecutorService bizExecutor(GracefulShutdownRegistry registry) {
    ExecutorService es = new ThreadPoolExecutor(...);
    registry.register(es);   // 关闭时按统一定义等待
    return es;
}
```

### 5.3 监听全局异常事件

`GlobalExceptionEvent` 会在统一异常处理器中发布，业务可订阅做埋点 / 告警：

```java
@EventListener
public void onGlobalException(GlobalExceptionEvent event) {
    GlobalExceptionEventPayload payload = event.getPayload();
    alarmService.send(payload.getErrorCode(), payload.getMessage(), payload.getStackTrace());
}
```

---

## 6. 与其他模块协作

| 模块                                      | 协作方式                                                                                 |
|-----------------------------------------|--------------------------------------------------------------------------------------|
| `ddf-common-api`                        | core 的上游；`BaseSign` / `ResponseData` / `ITreeTagCollection` / `PageResult` 等协议对象     |
| `ddf-common-mvc`                        | 复用 `SpringContextHolder` + `GlobalExceptionEvent` 做全局异常处理与日志增强                       |
| `ddf-common-authentication`             | 使用 `SecureUtil` / `SignatureUtil` 完成 Token / 验签                                      |
| `ddf-common-redis`                      | `ApplicationNamedKeyGenerator` 通过 `SpringContextHolder` 读取 `spring.application.name` |
| `ddf-common-data-mysql-starter`         | 复用 `BaseDomain`、`PageUtil`、`IdsUtil`，并接管线程池治理                                        |
| `ddf-common-ids-service`                | 提供集中式 ID 分发；`IdsUtil` 雪花是它的本地兜底                                                      |
| `ddf-common-limit` / `ddf-common-alarm` | 通过 `SpringContextHolder` 拿到 RedisTemplate 等基础 Bean                                   |

---

## 7. 常见问题（FAQ）

**Q1：调用 SecureUtil 报 `IllegalStateException: rsa private key is blank` ？**  
说明 `customizer.infra.global-properties.rsa-private-key` 未配置。生产环境建议从环境变量或 KMS
动态注入，而不是落到 `application.yml`。

**Q2：多实例 ID 重复，怎么排查？**  
检查每个实例进程的 `customizer.infra.global-properties.snowflake-worker-id` 是否互不相同。
K8s 部署建议结合 `metadata.uid` / Pod 序号生成 workerId；高并发场景请改用 `ddf-common-ids-service`。

**Q3：BeanCopierUtils 拷贝后字段为 null？**  
CGLIB 严格按字段名 + 完全一致的类型匹配，`int ↔ Integer`、`Long ↔ String` 都会被跳过。
请统一字段类型，或在拷贝后补充手工赋值。

**Q4：ThreadBuilderHelper 的指标在哪里看？**  
日志关键字 `[ThreadPoolMonitor]`，默认每 60 秒打印一次。如需接 Prometheus，请在 `ddf-common-governance-starter`
里启用 Micrometer 指标采集，自动包含 `executor.*` 指标。

**Q5：可以在非 Spring 环境使用这些工具吗？**

- `SpringContextHolder.getBeanWithStatic(...)`：可，返回 null
- `IdsUtil` / `SecureUtil` / `SignatureUtil`：依赖 `GlobalProperties` Bean，**无法在纯单测环境直接用**，
  需要手动构造 `GlobalProperties` 并塞入 `SpringContextHolder`，或通过 `@SpringBootTest` 启动上下文。

**Q6：`@RefreshScope` 让我担心配置刷新瞬间的不一致，怎么办？**  
雪花 ID 的 workerId / dataCenterId **不建议**热刷新（会导致刷新后产生冲突 ID）。
如果运行时确实需要调整，建议滚动重启实例。

---

## 8. 参考

- 源码：`config/GlobalProperties.java`、`config/CoreAutoConfiguration.java`
- 源码：`helper/SpringContextHolder.java`、`helper/ThreadBuilderHelper.java`
- 源码：`util/IdsUtil.java`、`util/SecureUtil.java`、`util/SignatureUtil.java`
- 源码：`util/BeanCopierUtils.java`、`util/PageUtil.java`、`util/TreeConvertUtil.java`、`util/LocalCacheUtil.java`
- 源码：`promise/DeferredHelper.java`、`promise/CompletableFutureHelper.java`
- Hutool 文档：<https://hutool.cn/docs/>
