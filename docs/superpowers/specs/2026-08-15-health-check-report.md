# ddf-common 全方位体检报告

> 日期：2026-08-15
> 范围：按根 `pom.xml` 模块顺序，覆盖全部 32 个模块（纯聚合的 dependency/starter-web/starter-default、纯配置的 log4j 跳过）
> 方法：结合 code-review-graph 全局风险信号（hub/bridge/gaps）+ 逐模块源码精读
> 前提：**不考虑向后兼容性**，允许激进重构

---

## 一、总览

共发现 **CRITICAL 5 项、HIGH 17 项、MEDIUM 24 项、LOW 22 项**。核心问题集中在三类：

1. **可直接利用的安全漏洞**：验证码校验绕过、硬编码云凭证、内置默认私钥、无鉴权管理控制台、反序列化 RCE 面。
2. **认证/签名的系统性缺陷**：AES-ECB 加密 token、签名默认关闭 + 后门 + 无 nonce 去重、日志泄露凭证。
3. **大量资源泄漏与死代码**：非 daemon 线程池、`System.out`/`main` 残留、调试遗留。

---

## 二、CRITICAL（立即处理）

| # | 模块          | 位置                                      | 问题                                                                           | 建议                                                                       |
|---|-------------|-----------------------------------------|------------------------------------------------------------------------------|--------------------------------------------------------------------------|
| 1 | captcha     | `CaptchaHelper.check():163-189`         | TEXT/MATH 验证码**完全不比对答案**，直接签发二次校验凭证，任意 verifyCode 都能通过                       | check 中对 TEXT/MATH 取出缓存答案做 equals 比对、比对后删除，失败抛 `VERIFY_CODE_NOT_MAPPING` |
| 2 | third-party | `oss/helper/OssHelper.java:230-238`     | `main` 方法硬编码阿里云 STS 的 accessKeyId/secret/securityToken/bucket，疑似真实凭证泄露       | 立即吊销该凭证 + 删除 main                                                        |
| 3 | ons         | `BaseOnsListenerContainer.java:159-165` | `toString()` 拼接 `secretKey`，start/destroy 都 info 输出 → AccessKey Secret 明文进日志 | 从 toString 移除 secretKey，日志脱敏                                             |
| 4 | websocket   | `WsSecureUtil.java:34-47`               | 未配置 RSA 密钥时回退**硬编码默认私钥**，握手 token 可被任何人伪造                                    | 删除内置默认私钥，强制要求配置密钥                                                        |
| 5 | ons         | `OnsConsoleController.java:58-164`      | 管理控制台鉴权仅比对请求体客户端自带的 `currentUser`，且硬编码为 `""`，无真实认证                           | 接入真实认证/授权链路，删除该控制器或彻底重构                                                  |

---

## 三、HIGH（优先处理）

### 认证 / 加密 / 签名

| #  | 模块             | 位置                                     | 问题                                                                                   | 建议                                          |
|----|----------------|----------------------------------------|--------------------------------------------------------------------------------------|---------------------------------------------|
| 6  | core           | `SecureUtil.java:91`                   | AES 用 `SymmetricCrypto(AES, key)` 默认 ECB/PKCS5Padding（无 IV 无 MAC），token 可预测/可伪造      | 改 AES-GCM（随机 IV）或加 HMAC                     |
| 7  | core           | `DefaultTokenGenerator.java:85-86`     | `log.error` 打印完整原始 token + 全堆栈                                                       | 不打印 token，堆栈改 `e.toString()`                |
| 8  | core           | `VerifyCodeUtil.java:49`               | `new Random(System.currentTimeMillis())` 时间戳做种子，验证码可预测                               | 用 `SecureRandom`                            |
| 9  | authentication | `AuthenticationProperties.java:32`     | `secret` 字段声明"必须配置"但**全仓库无引用**（真实密钥来自 GlobalProperties.aesSecret），配错不生效              | 删除该死字段或真正接入                                 |
| 10 | authentication | `AuthenticateTokenFilter.java:222-228` | `signEnabled` 默认 false 跳过签名；`mockSignEnabled`+`mockSign` 提供静态万能 sign 后门；nonce 无服务端去重 | 默认开启签名、去掉 mock sign、引入服务端 nonce 缓存          |
| 11 | core/mvc       | `SignatureUtil.java:195-252`           | HMAC 比较用 `Objects.equals`（时序侧信道）；重放校验只判时间戳下界、无上界、无 nonce 去重                          | 用 `MessageDigest.isEqual`；时间戳上下界 + nonce 去重 |

### 数据 / 序列化 / 日志泄露

| #  | 模块    | 位置                                               | 问题                                                                                    | 建议                                                    |
|----|-------|--------------------------------------------------|---------------------------------------------------------------------------------------|-------------------------------------------------------|
| 12 | redis | `RedisCustomizeAutoConfiguration.java:76-81,132` | `GenericJackson2JsonRedisSerializer()`/`JsonJacksonCodec()` 默认 typing 开启，存在反序列化 RCE 面 | 显式 ObjectMapper + `BasicPolymorphicTypeValidator` 白名单 |
| 13 | api   | `ResponseData.java:107/120/132/142`              | `success()`/`empty()` 把 `message` 误填为 "200"（成功提示语被塞进 subMessage 且被掩码）                 | message 传成功提示语                                        |
| 14 | mvc   | `AbstractExceptionHandler.java:179-186`          | 异常日志打印完整 body/queryString/headers + 全堆栈，可能含密码/token                                   | body/header 脱敏或不记录                                    |
| 15 | alarm | `CodeExceptionNotify.java:87-89`                 | 把请求参数/请求体/请求头（含 Authorization/cookie）拼进告警发往第三方 webhook                                | 移除 header/body 或脱敏                                    |

### 功能缺陷 / 资源

| #  | 模块           | 位置                                            | 问题                                                                         | 建议                                       |
|----|--------------|-----------------------------------------------|----------------------------------------------------------------------------|------------------------------------------|
| 16 | xxl-executor | `handler/SampleXxlJob.java:73-232`            | 官方示例任务（`ProcessBuilder` 执行任意命令、任意 URL 发请求 SSRF）以 `@Component` 随库注册         | 移除示例 Bean，改为文档片段                         |
| 17 | mqtt         | `EmqController.java:109-160`                  | 认证/superuser 仅凭 clientId 前缀匹配，前缀可伪造绕过；`clientIdPrefix` 为 null 时 NPE        | 接入真实凭证校验                                 |
| 18 | vps          | `VpsClient.java:66-67` + `VpsUtil.java:68-71` | 路径遍历防护被注释；`ProcessBuilder("sh","-c",...)` 拼接 filePath（可为 URL）→ 命令注入 + SSRF | 恢复防护；改用参数数组、禁止远程 URL                     |
| 19 | ids-service  | `IDAllocMapper.java:27-28`                    | `@Param("leafAlloc")` 但 SQL 用 `#{step}`/`#{key}`，动态步长触发 `BindingException` | 改 `#{leafAlloc.step}`/`#{leafAlloc.key}` |
| 20 | ids-service  | `SegmentIDGenImpl.java:68-69`                 | `ThreadPoolExecutor(5, Integer.MAX_VALUE, ...)` 最大线程无界                     | 改有界线程池                                   |
| 21 | zookeeper    | `MonitorRegistryConfig.java:77-82`            | `newClient(...)` 后**从未 `client.start()`**，后续操作抛 "instance must be started" | 补 start + close/@PreDestroy              |
| 22 | rocketmq     | `EnhanceMessageHandler.java:198-199`          | `sendResult` 为 null 时仍调 `getSendStatus()` 触发 NPE                           | 先判空                                      |
| 23 | limit        | `IpRateLimitKeyGenerator.java:33`             | 限流 key 依赖 `WebUtil.getHost()` 信任 `X-Forwarded-For`，可伪造 IP 绕过限流             | 可信代理真实 IP                                |

---

## 四、MEDIUM（应处理）

### 并发 / 线程 / 资源泄漏

| #  | 模块           | 位置                                                      | 问题                                                                 | 建议                                  |
|----|--------------|---------------------------------------------------------|--------------------------------------------------------------------|-------------------------------------|
| 24 | core         | `HttpClientUtil.java:186`、`ThreadBuilderHelper.java:68` | `newSingleThreadScheduledExecutor` 非 daemon 且永不 shutdown，阻止 JVM 退出 | daemon 线程工厂                         |
| 25 | core         | `CompletableFutureHelper`/`DeferredHelper`              | `synchronized(requestId.intern())` 污染字符串池 + 每实例各建线程池不关闭            | `ConcurrentHashMap.compute` + 共享线程池 |
| 26 | alarm        | `TableScan.java:58,129-167`                             | 非 daemon 定时器 + Statement/ResultSet 未关闭；裸 JDBC + 自动 DDL 建表          | daemon + 关闭语句 + 走 DataSource + 显式开关 |
| 27 | zookeeper    | `MonitorRegistryConfig.java:271-294`                    | 两个非 daemon 定时器 + client 未关闭                                        | daemon + 关闭                         |
| 28 | netty-broker | `ServerInboundHandler.java:94-99,51-55`                 | 首条消息重复入队；`channelInactive` 未清 `channelStore` 内存泄漏                  | 修复入队逻辑 + 清理 store                   |

### 并发 / 竞态 / 边界

| #  | 模块               | 位置                                                                     | 问题                                                                                     | 建议                        |
|----|------------------|------------------------------------------------------------------------|----------------------------------------------------------------------------------------|---------------------------|
| 29 | limit            | `RedisRepeatableValidator.java:70-74,63`                               | `setIfAbsent` 返回 null（连接异常）被当 true 放行（失败开放）；匿名无 IMEI 时 NPE                             | 区分 null/false；判空兜底        |
| 30 | limit            | `RateLimitAspect.java:138-139`                                         | `int == Integer` 拆箱 + `getRate()` null 时 NPE，全局回退逻辑无法区分"未设置"与"相等"                      | 注解未设(0)才回退全局              |
| 31 | limit            | `LocalRepeatableValidator.java:56-67`                                  | get→判断→put 非原子，并发 TOCTOU 双双放行                                                          | 原子操作                      |
| 32 | distributed-lock | `RedisDistributedLock.java:73-143`                                     | `finally` 中 `unlock()` 未包 try/catch，租约过期后 unlock 抛异常覆盖业务结果                             | 包 try/catch               |
| 33 | distributed-lock | `ZookeeperDistributedLock.java:33-34`                                  | `@Value(spring.profiles.active)` 多 profile 时路径含逗号非法；`acquire` 吞中断未恢复                   | 显式配置 + 恢复中断               |
| 34 | redis            | `RedisCommandHelper.java:1575-1592`                                    | `hMultiMapGetAll` 把 byte[] 强转 String 迭代 `ClassCastException`                           | 手动反序列化（对齐 pipelineZRange） |
| 35 | redis            | `RedisTemplateHelper.java:633-645,154-181`                             | `split("-")` 后无分隔符越界；`tokenBucketRateLimitAcquire` 无 max/rate 校验，rate=0/null 触发 Lua 错误 | 加校验                       |
| 36 | sharding         | `SuffixFieldShardingAlgorithm.java:21-51`                              | 分片键 null NPE；表名无 `_` 时 `substring(0,-1)` 异常；range 只用下界                                 | 加空值/边界校验                  |
| 37 | governance       | `DefaultMailService.java:42`                                           | 形参 `cc` 却调 `setBcc`（抄送当密送）；`setText(content, true)` 用户输入 HTML 注入                       | 改 setCc + 转义              |
| 38 | mqtt             | `DefaultMqttPublishImpl.java:173-175`、`MqttAutoConfiguration.java:104` | `listenerMap` 未判空 NPE；密码未配置 NPE                                                        | 判空                        |

### 安全加固（中危）

| #  | 模块           | 位置                                             | 问题                                                           | 建议              |
|----|--------------|------------------------------------------------|--------------------------------------------------------------|-----------------|
| 39 | mvc          | `WebUtil.java:70-101`                          | `getHost()` 无条件信任 `X-Forwarded-For`/`X-Real-IP` 取第一个值，IP 可伪造 | 可信代理白名单         |
| 40 | api          | `JsonUtil.java:258`                            | `Visibility.ANY` 使私有字段无 getter 也被序列化，可能泄露内部字段                | 收敛可见性白名单        |
| 41 | websocket    | `DefaultHandshakeInterceptor.java:77-79`       | 请求已带 Servlet principal 时直接 `return true` 跳过握手认证              | 取消旁路或显式配置       |
| 42 | websocket    | `WebsocketSessionStorage.java:314-316,203-207` | 超时未清理占位内存泄漏；`hash delete` 把 JSON 串当 hashKey 传 API 误用         | 清理 + 只传 hashKey |
| 43 | s3           | `FileUploadHelper.java:248-249`                | 采用客户端可控 contentType 存储，可存储型 XSS                              | 内容嗅探/白名单        |
| 44 | third-party  | `OssHelper.java:212-224`                       | STS 策略把用户可控 platform/identity 拼进资源，含 `/`/`*` 可越权             | 严格校验输入          |
| 45 | rocketmq     | `EnhanceMessageHandler.java:155-162`           | 不抛异常且不重试时异常被吞默认 ACK，消息静默丢失                                   | 显式告警/死信         |
| 46 | ons          | `OnsProducer.java:357-391`                     | `isRetryable()` 恒 false、重试被注释，发送失败静默丢消息                      | 恢复重试 + 抛异常      |
| 47 | netty-broker | `KeyManagerFactoryHelper.java:79-90`           | 默认 SSL 上下文硬编码密码但 `validatePassword` 拒绝默认密码，`ssl=true` 启动必抛异常 | 真实证书配置          |

---

## 五、LOW（可选处理，代码味道）

主要类别：

- **生产代码残留 `main`/`System.out`/`printStackTrace`**：`SecureUtil:266`、`SignatureUtil:278`、`CityUtil:49`（生产方法体内 `System.out.println`）、`CompletableFutureHelper:160`、`BCryptPasswordEncoder:181`、`RandomExtUtil:296`、`JsonUtil:195-215`、`MessagePackUtil:66` 等。
- **调试遗留死代码**：`zookeeper/listener/CopyRandomList.java`（LeetCode 算法题整个误入生产模块）、`MonitorRegistryConfig.main()`（含硬编码外网地址 `www.snowball.fans:2181`）、`TokenUtil`（已弃用无引用可删）、`oss/OssBeanDefinitionRegistrar.java`（整文件注释）。
- **重复造轮子**：`core/encode/BCryptPasswordEncoder.java + BCrypt.java` 手写 vendor 了 Spring Security 的 BCrypt，建议直接依赖 `spring-security-crypto`。
- **硬编码/魔法数字**：`CityUtil.java:19` 硬编码巨大省市映射（且数据有误）；captcha `CacheAdapter.java:39-40` TTL 硬编码 5 分钟；`CommonExceptionAdvice.java:16` 硬编码 `basePackages`。
- **全局副作用**：`SnowflakeZookeeperHolder.java:58-59` `System.setProperty`；`DataMysqlAutoConfiguration.java:30` `System.setProperty`；`DingTalkUtil.java:37-40` 静态块抓 Bean（早于容器就绪永久失效）。
- **其它**：`ApplicationNamedKeyGenerator` 缺应用名时前缀统一 "unknown" 多应用冲突；`RedisCommandHelper` 暴露阻塞 `keys(pattern)`；`mongo`/`zookeeper` AutoConfiguration 缺 `@ConditionalOnBean` 守卫导致无依赖时启动失败；`PercontUtils`/`JsonUtil` 等注释掉的 main。

---

## 六、建议处理顺序

1. **第一批（安全止血，半天内）**：CRITICAL 5 项 + HIGH 中可直接利用的（#6 AES-ECB、#7 日志泄露 token、#12 Redis 反序列化、#16 xxl 示例、#17 mqtt 认证、#18 vps 注入、#21 zk 未 start）。
2. **第二批（系统性加固，1-2 天）**：签名/nonce 体系（#10 #11）、验证码答案泄露（#8）、响应体 message（#13）、资源泄漏批次（#24-28）。
3. **第三批（质量清理）**：死代码/`main`/`printStackTrace` 清理、重复造轮子替换、全局副作用收敛。

> 注：本报告所有结论均由代码逐行确认，非臆测。涉及 `third-party OssHelper` 的硬编码凭证（CRITICAL #2）建议**立即吊销**。
