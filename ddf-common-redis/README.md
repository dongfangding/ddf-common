# ddf-common-redis

> Redis infrastructure module: a curated layer on top of Spring Data Redis and Redisson that ships
> unified `RedisTemplate` beans, a command wrapper, atomic Lua scripts, rate limiters, bloom filters,
> business leaderboards, multi-datasource support and more — so application code no longer has to
> deal with serialization boilerplate, key prefixing, or concurrency primitives.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-redis` is **the** module responsible for distributed state in the ddf-common stack.
Reach for it when any of the following applies:

| Category | Typical Problem | What the Module Provides |
| ----- | ----- | ----- |
| Cache / read-heavy | Unified JSON serialization, no manual key prefixing | `redisTemplate` / `stringRedisTemplate` defaults + `ApplicationNamedKeyGenerator` |
| Rate limiting | Endpoint anti-abuse, SMS frequency, campaign throttling | `RedisTemplateHelper#sliderWindowAccess` / `tokenBucketRateLimitAcquire` / `leakyBucketRateLimitAcquire` |
| Counters | "Increment + TTL + ceiling check" needs to be atomic | `incrementKeyExpire` / `hashIncreaseCheck` / `stringIncrWithLimit` and 30+ companion Lua scripts |
| Business leaderboards | Weekly/monthly rankings where ties must order by submit time | `zSetAddWithMaxCheckSupportBiz` / `zSetRevRangeBizRankingQuery` |
| Penetration defense | Mass empty queries hit the DB | `RedisBloomFilter` distributed bloom filter (Redisson-backed) |
| Multi Redis datasources | Separate main / risk / cache instances | `customizer.infra.redis.extra-multi` spins up multiple `RedissonClient` / `RedisTemplate` from one block |
| Geo queries | "People nearby", "stores nearby" | `GeoHelper` over Redisson `RGeo` |
| Pub/Sub | Cross-instance config broadcasts, force-logout | `RedisTopic` over Redisson `RTopic` |

> ⚠️ This module does **not** wire up Spring Cache annotations (`@Cacheable`). For method-level caching,
> enable the built-in `spring-boot-starter-cache` yourself.

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-redis</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

Transitive dependencies:

- `spring-boot-starter-data-redis` (with Lettuce client)
- `redisson-spring-boot-starter`
- `hutool-core`, `guava`, `caffeine`

> If you only need the pure utility classes (e.g. `ApplicationNamedKeyGenerator`), mark the dependency
> as `<optional>true</optional>` so Redisson is not pushed to downstream consumers.

---

## 3. Minimal Configuration

The module reuses Spring Boot's native `RedisProperties` (prefix `spring.data.redis`). No custom
namespace is required to get running.

### 3.1 Standalone

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your_password
      database: 0
      timeout: 3s
```

### 3.2 Sentinel

```yaml
spring:
  data:
    redis:
      password: your_password
      database: 0
      sentinel:
        master: mymaster
        nodes: 10.0.0.1:26379,10.0.0.2:26379,10.0.0.3:26379
```

### 3.3 Cluster

```yaml
spring:
  data:
    redis:
      password: your_password
      cluster:
        nodes: 10.0.0.1:7001,10.0.0.2:7002,10.0.0.3:7003
```

`RedisCustomizeAutoConfiguration` auto-detects sentinel / cluster / standalone modes and injects
the same connection metadata into Redisson.

### 3.4 Auto-wired Beans

After startup, the following beans are available for injection out of the box:

| Bean | Type | Notes |
| ----- | ----- | ----- |
| `redisTemplate` | `RedisTemplate<Object, Object>` | Values use `GenericJackson2JsonRedisSerializer` |
| `stringRedisTemplate` | `StringRedisTemplate` | Uses the module's custom `ObjectStringRedisSerializer` (object ↔ String) |
| `redisTemplateHelper` | `RedisTemplateHelper` | High-level facade combining Lua scripts and Redisson primitives |
| `redisCommandHelper` | `RedisCommandHelper` | ~1980 lines of standard command wrappers covering every data structure |
| `geoHelper` | `GeoHelper` | Redisson `RGeo` helper |
| `redissonClient` | `RedissonClient` | Provided by Redisson starter; this module injects Codec + connection info via `RedissonAutoConfigurationCustomizer` |

---

## 4. Core API Guide

### 4.1 Unified Key Generation: `ApplicationNamedKeyGenerator`

To avoid collisions when multiple applications share a Redis instance, the convention is to prefix
every key with `spring.application.name`:

```java
// Assume spring.application.name=user-service
String key = ApplicationNamedKeyGenerator.genKey("user", "info", String.valueOf(userId));
// → user-service:user:info:1001

// Cross-application keys (e.g. global feature flags) use the "global" prefix
String globalKey = ApplicationNamedKeyGenerator.genKey(true, "config", "feature-flag");
// → global:config:feature-flag

// Fully custom namespaces (e.g. third-party integrations)
String normal = ApplicationNamedKeyGenerator.genNormalKey("third-party", "wechat", openId);
// → third-party:wechat:xxx
```

The recommended pattern is **enum + template** for keys owned by a module:

```java
public enum UserRedisKeyEnum implements RedisKeyConstraint {

    USER_INFO("user:info:%s", RedisKeyTypeEnum.STRING, UserInfo.class),
    LOGIN_TOKEN("user:token:%s", RedisKeyTypeEnum.STRING),
    LOGIN_FAIL_COUNT("user:login-fail:%s", RedisKeyTypeEnum.STRING),
    ;

    @Getter private final String template;
    @Getter private final RedisKeyTypeEnum keyType;
    @Getter private final Class<?> clazz;

    UserRedisKeyEnum(String template, RedisKeyTypeEnum keyType) {
        this(template, keyType, null);
    }

    UserRedisKeyEnum(String template, RedisKeyTypeEnum keyType, Class<?> clazz) {
        this.template = template;
        this.keyType = keyType;
        this.clazz = clazz;
    }

    @Override
    public String getKey(Object... params) {
        return String.format(template, params);
    }
}
```

> `RedisKeyConstraint` lives in `ddf-common-core`. See the [core module key conventions](../ddf-common-core/CLAUDE.md).

### 4.2 Standard Commands: `RedisCommandHelper`

`RedisCommandHelper` wraps `StringRedisTemplate` to **hide byte/serialization detail and normalize
return types**. It covers every Redis data structure across ~1980 lines:

```java
@Resource
private RedisCommandHelper redisCommandHelper;

// String
redisCommandHelper.setEx("user:token:1001", "abc-token", 3600, TimeUnit.SECONDS);
String token = redisCommandHelper.get("user:token:1001");
boolean acquired = redisCommandHelper.setIfAbsent("user:lock:1001", "v", Duration.ofSeconds(10));
long count = redisCommandHelper.incrBy("user:visit:1001", 1L);

// Hash
redisCommandHelper.hPut("user:profile:1001", "nickname", "Tom");
String nickname = redisCommandHelper.hGet("user:profile:1001", "nickname");
Map<String, String> all = redisCommandHelper.hScan("user:profile:1001", "*");

// ZSet
redisCommandHelper.zAdd("leaderboard:week", "user:1001", 99.5);
Set<TypedTuple<String>> top10 = redisCommandHelper.zReverseRangeWithScores("leaderboard:week", 0, 9);

// Pipeline batch
redisCommandHelper.executePipelined(conn -> {
    for (Long id : userIds) {
        conn.stringCommands().get(("user:info:" + id).getBytes());
    }
    return null;
});
```

For the full method catalog, browse `RedisCommandHelper.java`.

### 4.3 Distributed Rate Limiting: `RedisTemplateHelper`

#### 4.3.1 Sliding Window

Best for "at most Y calls in X seconds" style throttling:

```java
@Resource
private RedisTemplateHelper redisTemplateHelper;

public void sendSms(String mobile) {
    AccessLimitResponse resp = redisTemplateHelper.sliderWindowAccess(
        "sms:send:" + mobile,
        5,    // up to 5 calls
        60    // within 60 seconds
    );
    if (resp.isLimited()) {
        throw new BusinessException("SMS sent too frequently, please retry later");
    }
    // perform the real send
}
```

The response carries the current hit count and the max — handy for showing "remaining attempts" to
the user.

#### 4.3.2 Token Bucket (Hash-backed, no Redisson dependency)

Best for steady-rate consumption such as OpenAPI gateways:

```java
boolean acquired = redisTemplateHelper.tokenBucketRateLimitAcquire(
    RateLimitRequest.builder()
        .key("api:rate:" + userId)
        .max(100)   // bucket capacity
        .rate(10)   // refill 10 tokens/sec
        .build()
);
if (!acquired) {
    throw new BusinessException("Request rate exceeded");
}
```

> `RateLimitRequest.builder()` automatically prefixes keys with `rate_limit:`.
> Call `.ignorePrefix(true)` when you have built your own namespace upstream.

#### 4.3.3 Leaky Bucket (Redisson `RRateLimiter`)

Best when you want a constant outflow without burst pile-up:

```java
boolean acquired = redisTemplateHelper.leakyBucketRateLimitAcquire(
    LeakyBucketRateLimitRequest.builder()
        .key("login:leaky:" + ip)
        .rate(10)                      // refill 10 tokens per interval
        .rateIntervalSeconds(1)        // interval = 1 second
        .permits(1)                    // request 1 permit
        .build()
);
```

### 4.4 Counters with Boundary Checks

#### 4.4.1 Increment with TTL (set expiry on first call)

```java
// Limit a user to 100 posts per day
long current = redisTemplateHelper.incrementKeyExpire(
    "user:post-count:" + LocalDate.now(),
    1,                      // step
    86400                   // set TTL = 1 day on first increment
);
if (current > 100) {
    throw new BusinessException("Daily post limit reached");
}
```

#### 4.4.2 Hash Increment with Atomic Rollback

The Lua script below guarantees that exceeding the limit also rolls back the increment, even under
concurrent traffic:

```java
HashIncrementCheckResponse resp = redisTemplateHelper.hashIncreaseCheck(
    "user:quota:" + userId,
    "monthly-export",
    1,
    50    // monthly export ceiling = 50
);
if (!resp.isSuccess()) {
    throw new BusinessException(
        "Monthly export limit (" + resp.getLimitValue() + ") reached");
}
```

#### 4.4.3 Increment That Throws on Overflow

When hitting the ceiling should immediately abort the business flow, use the `*CheckException`
variants:

```java
redisTemplateHelper.stringIncrWithLimitCheckException(
    "ip:attempt:" + clientIp,
    1,
    10,                              // ceiling = 10
    300,                             // TTL = 300 seconds
    ErrorCodeEnum.LOGIN_TOO_FREQUENT // BusinessException thrown on overflow
);
```

### 4.5 Safe Delete (CAS-style)

Avoid the classic "read → compare → delete" race:

```java
boolean removed = redisTemplateHelper.stringDeleteWithCheckValue(
    "session:" + sessionId, expectedToken);
boolean hashRemoved = redisTemplateHelper.hashDeleteWithCheckValue(
    "user:lock", userId, expectedOwner);
```

### 4.6 Business Leaderboards (Stable Order on Score Ties)

The helper ships a set of zset utilities that handle the classic "tie-break by submit time" problem
by packing milliseconds into the fractional part of the score:

```java
// Earlier submissions rank higher when scores tie
redisTemplateHelper.zSetAddWithMaxCheckSupportBiz(
    ZSetAddDoubleWithMaxCheckCommand.builder()
        .key("leaderboard:weekly")
        .member("user:1001")
        .score(99.5)
        .build()
);

// Top N
ZRevRangeBizRankingResponse top = redisTemplateHelper.zSetRevRangeBizRankingQuery(
    ZRevRangeBizRankingQuery.builder()
        .key("leaderboard:weekly")
        .start(0L)
        .end(9L)
        .build()
);

// Rank around a target user (3 above + target + 3 below)
RankResponse around = redisTemplateHelper.rankAround(
    "leaderboard:weekly", "user:1001", 3);
```

### 4.7 Bloom Filter

```java
RedisBloomFilter<String> filter = redisTemplateHelper.createRedisBloomFilter(
    "user:registered",
    1_000_000,    // expected insertions
    0.001         // tolerated false-positive rate (0.1%)
);

filter.add("13800138000");
boolean maybeExists = filter.contains("13800138000");
```

> Bloom filters **must be seeded** with the existing dataset, otherwise even the "definitely not
> present" guarantee no longer holds. Typically you seed on application startup or after data
> mutations.

### 4.8 Pub/Sub

```java
RedisTopic topic = RedisTopic.newInstance("config:change", redissonClient);
topic.addListener(ConfigChangeEvent.class, (channel, event) -> {
    log.info("Received config change {}", event);
    configCache.invalidate(event.getKey());
});

topic.publish(new ConfigChangeEvent("feature.new-ui", "true"));
```

---

## 5. Advanced Usage / Extension Points

### 5.1 Multiple Redis Datasources

When you need separate Redis clusters for the main app, risk control, and a cache tier, enable
`customizer.infra.redis.extra-multi`:

```yaml
spring:
  data:
    redis:
      host: 10.0.0.1
      port: 6379
      password: main-pass

customizer:
  infra:
    redis:
      extra-multi:
        enable: true
        map:
          risk:
            host: 10.0.0.2
            port: 6379
            password: risk-pass
            database: 0
          cache:
            host: 10.0.0.3
            port: 6379
            password: cache-pass
            database: 1
```

For each map entry, the module registers **prefixed beans**:

- `riskRedissonClient` / `riskRedissonConnectionFactory` / `riskStringRedisTemplate` / `riskRedisTemplate` / `riskRedisCommandHelper`
- `cacheRedissonClient` / `cacheRedissonConnectionFactory` / `cacheStringRedisTemplate` / `cacheRedisTemplate` / `cacheRedisCommandHelper`

```java
@Resource(name = "riskRedisCommandHelper")
private RedisCommandHelper riskRedis;
```

> Multi-datasource mode currently supports **standalone** connections only — sentinel/cluster is not
> handled here.

### 5.2 Custom Redisson Codec

```yaml
spring:
  redis:
    redisson:
      codec: org.redisson.codec.Kryo5Codec
```

When omitted, `JsonJacksonCodec` is used.

### 5.3 Registering Custom Lua Scripts

Mirror the pattern in `RedisLuaScript`: drop the script under `classpath:lua/xxx.lua`, declare a
`RedisScript.of(...)` constant, then invoke it via `stringRedisTemplate.execute(script, keys, args)`.
The module ships 30+ scripts already:

| Category | Script Constants |
| ----- | ----- |
| Rate limiting | `TOKEN_BUCKET_RATE_LIMIT`, `SLIDER_WINDOW_COUNT` |
| Counters | `STRING_KEY_INCREMENT_EXPIRE`, `STRING_KEY_INCREMENT_EXPIRE_AT`, `STRING_INCREMENT_CHECK`, `STRING_TTL_INCR_WITH_LIMIT` |
| Hash | `HASH_INCREMENT_CHECK`, `HASH_DECREMENT_CHECK`, `HASH_BATCH_INCREMENT_CHECK`, `MULTIPLE_HASH_BATCH_INCREMENT_CHECK`, `HASH_INCREMENT_PERSIST_LIMIT_VALUE`, `HASH_INCREASE_ROUNDING_REDUCE`, `HASH_INCR_WITH_FIRST_SET_TTL`, `HASH_INCR_FLOAT_ROUND_DECIMAL`, `HASH_DECREASE_UNTIL_FIRST_LESS_THAN_ZERO`, `HASH_VALUE_UPDATE_SELECTIVE` |
| Safe delete | `HASH_DELETE_WITH_CHECK_VALUE`, `STRING_DELETE_WITH_CHECK_VALUE` |
| ZSet leaderboards | `ZSET_INCR_WITH_TIME`, `ZSET_ZADD_WITH_MAX_CHECK`, `ZSET_ZADD_WITH_TIME_MAX_CHECK`, `ZSET_AROUND_ELEMENT_RANK`, `ZSET_REV_RANGE_BIZ_RANKING_QUERY`, `ZSET_REV_RANGE_USER_BIZ_RANKING_ELEMENT_QUERY`, `ZSET_DELETE_WITH_MAX_SCORE_CHECK`, `ZSET_RANGEBYSCORE_ZREM` |
| Misc | `MAX_CAPACITY_HISTORY_CONTAINER`, `MAX_ELEMENT_DICT` |

---

## 6. Interplay with Other Modules

| Module | How They Cooperate |
| ----- | ----- |
| `ddf-common-core` | Supplies `RedisKeyConstraint`, `BusinessException`, `SpringContextHolder` (used by `ApplicationNamedKeyGenerator` to read `spring.application.name`) |
| `ddf-common-limit` | Annotation-based rate limiting (`@RateLimit`) delegates to `RedisTemplateHelper.sliderWindowAccess` |
| `ddf-common-alarm` | Alarm dedup / suppression windows rely on `AlarmRedisKeyEnum` + `hashIncreaseCheck` |
| `ddf-common-authentication` | Token / captcha storage; login-failure counters typically use `stringIncrWithLimitCheckException` |
| `ddf-common-mq` | Cross-instance broadcasts can ride on `RedisTopic` instead of pulling in a full message broker |

---

## 7. FAQ

**Q1: Mojibake when reading values?**  
The default `stringRedisTemplate` uses UTF-8. If business code accidentally falls back to the JDK
default `RedisTemplate` with `JdkSerializationRedisSerializer`, keys end up binary-serialized and
`redis-cli` shows control characters. Always go through `stringRedisTemplate` or
`redisTemplateHelper`.

**Q2: Why can `hashIncreaseCheck` still tick up after hitting the ceiling?**  
The Lua script follows "increment → compare → rollback" semantics. The transient increment exists,
but the script rolls the field back to `limit` if it overflows — **the persisted value never
exceeds the ceiling**, though the returned `currentValue` may equal `limit` instead of `limit-1`.

**Q3: Why are scores in business leaderboards fractional?**  
To break ties by submit time, the helper packs `9999999999 - timestamp` into the decimal part of
the score. Earlier submissions get a larger decimal and therefore rank higher overall.
`zSetRevRangeBizRankingQuery` automatically restores the original business score in the response.

**Q4: Can I mix `RedisTemplate` with module-managed keys?**  
Yes. The module's `redisTemplate` uses `GenericJackson2JsonRedisSerializer`, compatible with the
JSON payloads produced by `RedisTemplateHelper`. However, **do not** mix `RedisTemplate` and
`StringRedisTemplate` on the same key — the differing serializers will produce read failures.

**Q5: How should I tune the connection pool?**  
Both Redisson and Lettuce have their own pool config:

- Lettuce: `spring.data.redis.lettuce.pool.*`
- Redisson: `spring.data.redis.redisson.config` (YAML blob) + `spring.redis.redisson.codec`

For high concurrency, target `connectionPoolSize` ≥ 64 and `connectionMinimumIdleSize` ≥ 24.

**Q6: How to disable this module's auto-configuration?**  
Exclude the auto-config classes explicitly:

```yaml
spring:
  autoconfigure:
    exclude:
      - com.ddf.boot.common.redis.config.RedisCustomizeAutoConfiguration
      - com.ddf.boot.common.redis.config.ExtraRedissonAutoConfiguration
```

---

## 8. References

- Source: `RedisTemplateHelper.java` — high-level, Lua-script-backed API
- Source: `RedisCommandHelper.java` — standard command wrapper
- Redisson config docs: <https://github.com/redisson/redisson/wiki/2.-Configuration>
- Spring Data Redis reference: <https://docs.spring.io/spring-data/redis/reference/>
