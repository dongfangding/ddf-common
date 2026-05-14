# ddf-common-core

> Generic kernel module: on top of `ddf-common-api`, this ships a curated set of **business-agnostic
> utilities** — Spring context access, thread-pool governance, snowflake IDs, crypto/signature,
> local caching, bean copy, tree assembly, pagination bridging, and more. Almost every other
> ddf-common module depends on it transitively; **application services rarely need to declare it
> directly**.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

In the four-layer architecture `ddf-common-core` lives in the **kernel layer**. It supplies the
Spring/JDK-level common infrastructure that everything above it builds on, but it does **not**
ship a database, Redis, MQ, or any other concrete infrastructure — it gives those infrastructure
modules a shared foundation.

| Category                              | Typical Problem                                                     | What the Module Provides                                             |
|---------------------------------------|---------------------------------------------------------------------|----------------------------------------------------------------------|
| Spring bean access from non-bean code | A `static` helper needs `RedisTemplate`                             | `SpringContextHolder.getBeanWithStatic(...)` with silent degradation |
| Distributed snowflake IDs             | workerId / dataCenterId must be unique per instance                 | `IdsUtil.getNextLongId()` + `customizer.infra.global-properties`     |
| RSA / AES / HMAC                      | Gateway signing, link-layer encryption, at-rest encryption          | `SecureUtil` + `rsa-*`/`aes-secret`/`sign-secret`                    |
| Request signatures                    | ASCII-sorted, nested-DTO-flattened HMAC signing                     | `SignatureUtil` over `BaseSign`                                      |
| Thread-pool governance                | Graceful shutdown, leak detection, unified metrics                  | `ThreadBuilderHelper.buildThreadPoolTaskExecutor` (auto-registered)  |
| Tree assembly                         | Department / menu / region trees                                    | `TreeConvertUtil.convert(...)`                                       |
| Pagination bridging                   | Convert between MyBatis PageHelper and Spring Data `Pageable`       | `PageUtil`                                                           |
| Bean copy                             | Hot path DTO ↔ entity conversion                                    | `BeanCopierUtils` (CGLIB BeanCopier + ReflectASM constructor cache)  |
| Promise / blocking await              | "Place order, then wait for the async callback, time out otherwise" | `DeferredHelper` / `CompletableFutureHelper`                         |
| Environment awareness                 | prod/dev/test detection, skip-on-profile                            | `EnvironmentHelper`                                                  |
| Local cache                           | High-frequency, low-cardinality reads without distributed coherence | `LocalCacheUtil` (Caffeine + Guava + Hutool `TimedCache`)            |

> ⚠️ JDBC / Druid / Mail / Actuator implementations have **already been moved out** of core into
> dedicated starters. If you need those, depend on `ddf-common-data-mysql-starter` /
> `ddf-common-governance-starter`.

---

## 2. Maven Dependency

In most cases you should **not** depend on `ddf-common-core` directly. The composite starters
(`ddf-common-starter-web` / `ddf-common-starter-default`) pull it in transitively. Declare it
explicitly only when you need a specific utility (e.g. `IdsUtil`, `SecureUtil`) from a leaf
library project:

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-core</artifactId>
    <version>${ddf-common.version}</version>
    <!-- Recommended: optional, so Spring / Hutool / Caffeine are not pushed downstream -->
    <optional>true</optional>
</dependency>
```

Major transitive dependencies:

- `ddf-common-api` (protocol root: `BaseSign` / `ITreeTagCollection` / `ResponseData`, …)
- `spring-cloud-context` (provides `@RefreshScope` so `GlobalProperties` is hot-reloadable)
- `spring-boot-starter-cache`, `caffeine`, `guava` (local caching)
- `hutool-core` / `crypto` / `cache` / `extra` / `http` / `json` — **do not** mix with `hutool-all`
- `aspectjweaver`, `httpclient5`, `jdeferred-core`, `reflectasm`

---

## 3. Minimum Configuration

All knobs are centralized in `GlobalProperties` under prefix **`customizer.infra.global-properties`**.
Every field is optional; configure only what you actually use:

```yaml
customizer:
  infra:
    global-properties:
      # Snowflake ID (workerId MUST be unique across instances)
      snowflake-worker-id: 1
      snowflake-data-center-id: 1

      # RSA key pair (PEM text). Required when calling SecureUtil#rsa* — otherwise IllegalStateException.
      rsa-private-key: |
        -----BEGIN PRIVATE KEY-----
        ...
        -----END PRIVATE KEY-----
      rsa-public-key: |
        -----BEGIN PUBLIC KEY-----
        ...
        -----END PUBLIC KEY-----

      # AES / HMAC keys (required when calling SecureUtil#aes* / SignatureUtil)
      aes-secret: ${AES_SECRET:}
      sign-secret: ${SIGN_SECRET:}

      # When true, BusinessException's code is mirrored to HTTP response.status
      exception-code-to-response-status: false

      # Verbose logging + log-suppression blacklist
      global-log-print-details: false
      ignore-log-exception-class-name:
        - org.springframework.web.servlet.NoHandlerFoundException
```

> The class carries `@RefreshScope`, so config-center pushes (Nacos / Apollo / SCG) followed by
> `POST /actuator/refresh` take effect immediately — but be careful with in-flight logic that was
> already mid-execution at the moment of refresh.

### 3.1 Auto-wired Beans

After startup `CoreAutoConfiguration` injects:

| Bean                                   | Type                                        | Notes                                                    |
|----------------------------------------|---------------------------------------------|----------------------------------------------------------|
| `globalProperties`                     | `GlobalProperties`                          | Hot-reloadable global knobs                              |
| `springContextHolder`                  | `SpringContextHolder`                       | Enables Hutool `SpringUtil`; provides static bean lookup |
| `environmentHelper`                    | `EnvironmentHelper`                         | App name / port / profile helpers                        |
| `threadPoolExecutorShutdownDefinition` | `ExecutorServiceGracefulShutdownDefinition` | Default: 120 s graceful shutdown                         |
| `deferredHelper`                       | `DeferredHelper<?, ?, ?>`                   | jdeferred-backed "await callback" helper                 |
| `completableFutureHelper`              | `CompletableFutureHelper<?>`                | Helpers around `CompletableFuture`                       |

Every bean carries `@ConditionalOnMissingBean` — override freely from your own configuration.

---

## 4. Core API Guide

### 4.1 SpringContextHolder — Access the container from non-bean code

```java
// Standard: throws NoSuchBeanDefinitionException when the bean is absent
RedisTemplate<?, ?> redisTemplate = SpringContextHolder.getBean(RedisTemplate.class);

// Silent degradation: returns null when the bean / context is missing
GlobalProperties props = SpringContextHolder.getBeanWithStatic(GlobalProperties.class);
if (props == null) { /* fall back to defaults or unit-test stub */ }

// Type + name combo
PrincipalFactory factory = SpringContextHolder.getBean("customPrincipalFactory", PrincipalFactory.class);

// Whole family
Map<String, RedisKeyConstraint> all = SpringContextHolder.getBeansOfType(RedisKeyConstraint.class);
```

> `SpringContextHolder` is enabled transitively via Hutool's `@EnableSpringUtil`. **As long as
> `ddf-common-core` is on the classpath**, no extra wiring is needed.

### 4.2 IdsUtil — Snowflake IDs

```java
long orderId = IdsUtil.getNextLongId();        // 64-bit numeric
String traceId = IdsUtil.getNextStrId();       // Stringified (handy for logs / Mongo)
```

Backed by Hutool `IdUtil.getSnowflake(workerId, dataCenterId)`, with values read from
`GlobalProperties`.

> ⚠️ **In a multi-instance deployment, `snowflake-worker-id` MUST differ per instance** (inject via
> ConfigMap / start-up arg, etc.); otherwise you risk ID collisions. For high-volume systems, prefer
> the centralized service in `ddf-common-ids-service` and treat the local snowflake as a fallback.

### 4.3 SecureUtil — RSA / AES

```java
// RSA (PKCS#8 PEM keys are eagerly loaded from GlobalProperties)
String signature = SecureUtil.rsaSign("payload-to-sign");
boolean ok = SecureUtil.rsaVerify("payload-to-sign", signature);

// Payload encryption
String cipher = SecureUtil.rsaEncryptByPublicKey("sensitive-data");
String plain  = SecureUtil.rsaDecryptByPrivateKey(cipher);

// AES
String aesCipher = SecureUtil.aesEncrypt("plain-text");
String aesPlain  = SecureUtil.aesDecrypt(aesCipher);

// Password hashing
String hash = SecureUtil.bcryptEncode("user-password");
boolean matches = SecureUtil.bcryptMatches("user-password", hash);
```

> Calling these methods without configured keys throws `IllegalStateException`. Inject private
> material via KMS / Vault / env vars into `customizer.infra.global-properties.rsa-private-key` —
> do not commit it to `application.yml`.

### 4.4 SignatureUtil — Request signing

```java
// Nested objects flattened, fields sorted by ASCII, salt appended, HMAC-SHA256 emitted
BaseSign sign = SignatureUtil.signature(requestDto);
httpHeaders.set("X-Signature", sign.getSign());
httpHeaders.set("X-Timestamp", String.valueOf(sign.getTimestamp()));

// Server-side verification
SignatureUtil.verify(requestDto, sign);   // throws BusinessException on mismatch
```

Internally `SignatureUtil` calls `asciiSortToQueryStringOnlyBasicType` to flatten payloads into
`a=1&b.c=2&b.d=3` form, so nested DTOs are signed stably.

### 4.5 ThreadBuilderHelper — Governed thread pools

```java
ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadPoolTaskExecutor(
        "order-callback",   // thread-name prefix (shows up in logs)
        4,                  // core
        16,                 // max
        500,                // queueCapacity
        60                  // keepAliveSeconds
);
executor.execute(() -> handleCallback(payload));
```

Pools built this way come with three guarantees:

1. **Auto-registered for graceful shutdown** — bound by `ExecutorServiceGracefulShutdownDefinition` (120 s default)
2. **Tracked in a static `POOLS` list** — periodic logger prints `active / queue / completed` counters
3. **Unified rejection policy** — defaults to `CallerRunsPolicy` so tasks aren't silently dropped

### 4.6 BeanCopierUtils — Fast bean copy

```java
UserDTO dto = BeanCopierUtils.copyProperties(userEntity, UserDTO.class);
List<UserDTO> list = BeanCopierUtils.copyListProperties(entities, UserDTO::new);
```

Caches CGLIB `BeanCopier` per source/target pair and constructs targets via ReflectASM
`ConstructorAccess` — an order of magnitude faster than pure reflection.

> ⚠️ Unlike Spring `BeanUtils.copyProperties`, **CGLIB does not auto-convert `int ↔ Integer`,
> `Long ↔ String`, etc.** — mismatching types are silently skipped. Keep field types aligned (e.g.
> with `BaseDomain` + Lombok) or perform a manual fix-up after copying.

### 4.7 TreeConvertUtil — Tree assembly

```java
// DTO implements ITreeTagCollection<Long, DeptDTO>
List<DeptDTO> flatList = deptMapper.listAll();
List<DeptDTO> tree = TreeConvertUtil.convert(flatList);

// Sorted children (relies on ITreeTagCollection#getSort)
List<DeptDTO> sortedTree = TreeConvertUtil.convertWithSort(flatList);
```

DTOs must implement `ITreeTagCollection<K, T>` to expose self-ID, parent-ID, and child container.

### 4.8 PageUtil — Pagination bridge

```java
// Service-side: PageRequest → PageHelper
PageHelper.startPage(PageUtil.toPageNum(query), query.getPageSize());
List<UserVO> list = userMapper.list(query);

// Return: PageInfo → unified PageResult
return PageUtil.buildPageResult(new PageInfo<>(list));
```

`PageResult<T>` comes from `ddf-common-api` and is consumed by frontends through a uniform shape.

### 4.9 LocalCacheUtil — Local cache

```java
// Caffeine with auto-expiry
String region = LocalCacheUtil.computeIfAbsent("region:" + cityCode,
        Duration.ofMinutes(10),
        () -> cityClient.queryRegion(cityCode));

// Guava LoadingCache: best for small key space + very high call frequency
LoadingCache<Long, UserBaseInfo> userCache = LocalCacheUtil.buildLoadingCache(
        Duration.ofMinutes(5), 5_000, userId -> userMapper.findById(userId));
```

> Local caches are **not** distributed-consistent. Use `ddf-common-redis` for state that must be
> shared across instances.

### 4.10 Async await — Deferred / CompletableFuture

```java
// Pattern: kick off async payment, block until callback or time out after 30 s
String txId = paymentClient.pay(request);
PaymentCallback callback = deferredHelper.acquire(txId, 30_000);   // blocks

// Resolver thread (HTTP / MQ / Webhook)
deferredHelper.resolve(txId, callback);
```

Both helpers manage the "business-ID ↔ Future" registry and timeout cleanup; the actual signal
(HTTP, MQ, Webhook) is delivered by your own code.

### 4.11 BaseDomain — Persistence base class

```java
@SuperBuilder(toBuilder = true)
@Data @EqualsAndHashCode(callSuper = true)
public class UserDO extends BaseDomain {
    private String username;
    private String mobile;
}
```

`BaseDomain` ships `id`, `gmtCreated`, `gmtModified` (millisecond timestamps) — keeps naming and
auditing consistent across all persistence-layer entities.

---

## 5. Advanced Usage / Extension Points

### 5.1 Custom graceful-shutdown window

Default is 120 s. Override the bean in your own `@Configuration`:

```java
@Bean
public ExecutorServiceGracefulShutdownDefinition threadPoolExecutorShutdownDefinition() {
    return new ExecutorServiceGracefulShutdownDefinition(30, TimeUnit.SECONDS);
}
```

### 5.2 Register a hand-rolled `ExecutorService` for graceful shutdown

If you didn't build the pool via `ThreadBuilderHelper`, register it manually:

```java
@Bean
public ExecutorService bizExecutor(GracefulShutdownRegistry registry) {
    ExecutorService es = new ThreadPoolExecutor(...);
    registry.register(es);   // awaited under the unified shutdown definition
    return es;
}
```

### 5.3 Subscribe to global exception events

`GlobalExceptionEvent` is fired from the unified exception handler — listen to it for
observability / alerting:

```java
@EventListener
public void onGlobalException(GlobalExceptionEvent event) {
    GlobalExceptionEventPayload payload = event.getPayload();
    alarmService.send(payload.getErrorCode(), payload.getMessage(), payload.getStackTrace());
}
```

---

## 6. Interplay with Other Modules

| Module                                  | How They Cooperate                                                                                         |
|-----------------------------------------|------------------------------------------------------------------------------------------------------------|
| `ddf-common-api`                        | Upstream of core: ships `BaseSign` / `ResponseData` / `ITreeTagCollection` / `PageResult`                  |
| `ddf-common-mvc`                        | Reuses `SpringContextHolder` + `GlobalExceptionEvent` for the global exception handler and request logging |
| `ddf-common-authentication`             | Uses `SecureUtil` / `SignatureUtil` for token issuance and signature validation                            |
| `ddf-common-redis`                      | `ApplicationNamedKeyGenerator` resolves `spring.application.name` through `SpringContextHolder`            |
| `ddf-common-data-mysql-starter`         | Reuses `BaseDomain`, `PageUtil`, `IdsUtil` and inherits thread-pool governance                             |
| `ddf-common-ids-service`                | Provides centralized ID dispatch; `IdsUtil` snowflake remains as local fallback                            |
| `ddf-common-limit` / `ddf-common-alarm` | Pull `RedisTemplate` and other infra beans through `SpringContextHolder`                                   |

---

## 7. FAQ

**Q1: `SecureUtil` throws `IllegalStateException: rsa private key is blank`. Why?**  
`customizer.infra.global-properties.rsa-private-key` is not set. In production, inject the value
via environment variable or a secret manager — do not store it in `application.yml`.

**Q2: How do I debug duplicate snowflake IDs across instances?**  
Check that every process has a distinct `customizer.infra.global-properties.snowflake-worker-id`.
For Kubernetes, derive workerId from `metadata.uid` / pod ordinal; for high-volume systems, use
`ddf-common-ids-service` instead.

**Q3: `BeanCopierUtils` left some fields null — why?**  
CGLIB requires exact field-name + exact type matches; `int ↔ Integer`, `Long ↔ String`, etc. are
silently skipped. Align field types or perform a manual fix-up after copying.

**Q4: Where do the `ThreadBuilderHelper` metrics show up?**  
Look for `[ThreadPoolMonitor]` in the logs (every 60 s by default). To export to Prometheus,
enable Micrometer integration via `ddf-common-governance-starter` — the default `executor.*` metrics
will be picked up automatically.

**Q5: Can I use these utilities outside Spring?**

- `SpringContextHolder.getBeanWithStatic(...)` — yes, returns `null`.
- `IdsUtil` / `SecureUtil` / `SignatureUtil` — they depend on `GlobalProperties`; you must either
  bootstrap a Spring context (`@SpringBootTest`) or construct `GlobalProperties` manually and
  inject it through `SpringContextHolder`.

**Q6: I'm worried about `@RefreshScope` causing transient inconsistency. What should I do?**  
`snowflake-worker-id` / `snowflake-data-center-id` should **not** be hot-refreshed — flipping them
at runtime can produce colliding IDs. Roll the instance instead if you must change them.

---

## 8. References

- Source: `config/GlobalProperties.java`, `config/CoreAutoConfiguration.java`
- Source: `helper/SpringContextHolder.java`, `helper/ThreadBuilderHelper.java`
- Source: `util/IdsUtil.java`, `util/SecureUtil.java`, `util/SignatureUtil.java`
- Source: `util/BeanCopierUtils.java`, `util/PageUtil.java`, `util/TreeConvertUtil.java`, `util/LocalCacheUtil.java`
- Source: `promise/DeferredHelper.java`, `promise/CompletableFutureHelper.java`
- Hutool reference: <https://hutool.cn/docs/>
