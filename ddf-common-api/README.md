# ddf-common-api

> API protocol and constraint root module. Holds all shared **response contracts, exception taxonomy,
> pagination models, signing interfaces, Redis key constraints, and tree structure contracts**
> used across the ddf-common stack. **Zero business infrastructure**, zero Spring beans.
> It is the dependency root of the entire ddf-common family; `ddf-common-core` and every downstream
> module depends on it directly.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-api` sits at the **bottom** of the four-layer architecture. When you need a set of
pure Java contracts that are shared across services without pulling in Spring infrastructure,
this is the first line of your `pom.xml`.

| Category                                  | Typical Problem                                                                 | What the Module Provides                                |
|-------------------------------------------|---------------------------------------------------------------------------------|---------------------------------------------------------|
| Unified cross-service response format     | Each service returns a different JSON shape; gateway aggregation is painful     | `ResponseData<T>` unified envelope                      |
| Unified cross-service exception semantics | Service A throws `USER_NOT_FOUND`, service B throws `用户不存在` — gateway can't map | `BaseCallbackCode` + `BaseErrorCallbackCode` enums      |
| Pagination protocol                       | MyBatis, JPA, Mongo each return different pagination structures                 | `PageRequest` interface + `PageResult<T>`               |
| Tamper-proof requests                     | Open gateway needs request signing; DTOs must carry signature fields            | `BaseSign` interface (`sign` + `nonceTimestamp`)        |
| Redis key governance                      | Keys are ad-hoc, formats are inconsistent, concatenation is error-prone         | `RedisKeyConstraint` template contract + sharding rules |
| Tree structures                           | Department / menu / region lists need recursive assembly                        | `ITreeTagCollection<K, T>` tree-node contract           |
| Serialization compatibility               | `LocalDateTime` / `Duration` behavior differs between frontend and backend      | Jackson customizations (JSR-310 + MsgPack)              |
| Sensitive data masking                    | Logs accidentally print mobile numbers / ID card numbers                        | `@SensitiveField` + masking serializer                  |

> ⚠️ This module **contains no** Spring beans, auto-configuration, database access, or Redis
> operations. For infrastructure, depend on `ddf-common-core` or a concrete starter.

---

## 2. Maven Dependency

As a pure protocol layer, this module is extremely lightweight:

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-api</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

Transitive dependencies:

- `spring-boot-starter-validation` (includes `jakarta.validation-api`, `hibernate-validator`)
- `jackson-datatype-jsr310`, `jackson-dataformat-msgpack`
- `pagehelper-spring-boot-starter` (pagination interface adaptation)
- `hutool-core`, `commons-lang3`, `commons-codec`, `guava`
- `lombok`

> No Spring Boot starter, no database driver, no Redis client, and no other heavy dependencies.

---

## 3. Core API Guide

### 3.1 Unified response: `ResponseData<T>`

```java
// Controller return (the global exception handler and AOP wrapper usually handle this automatically;
// business code normally returns raw objects directly)
ResponseData.success(userInfo);                              // success
ResponseData.success(userInfo, "Login successful");          // success + custom message
ResponseData.failure(BaseErrorCallbackCode.UNAUTHORIZED);    // failure
ResponseData.empty();                                        // empty response
```

Structure:

```json
{
  "code": "200",
  "message": "Request successful",
  "subMessage": "Detailed error (hidden in prod via @JsonIgnoreProfile)",
  "timestamp": 1700000000000,
  "data": { ... },
  "extra": { ... }
}
```

Convenience methods for consumers:

```java
// Hard assertion: if not success, rethrow as BusinessException from the response payload
UserInfo user = remoteUserService.get(userId).requiredSuccess();

// Failure fallback
UserInfo user = remoteUserService.get(userId).failureDefault(UserInfo.guest());
```

### 3.2 Exception taxonomy

#### 3.2.1 Error-code interface: `BaseCallbackCode`

```java
public enum OrderErrorCode implements BaseCallbackCode {
    ORDER_NOT_FOUND("ORDER_001", "Order not found"),
    ORDER_OVER_LIMIT("ORDER_002", "Daily order limit {0} reached", "Order limit exceeded");

    private final String code;
    private final String description;   // internal / dev use
    private final String bizMessage;    // user-facing toast; optional override
}
```

Every error code must provide `code` and `description`, and may optionally provide `bizMessage`
(used in production to hide system-level detail from users).

#### 3.2.2 Predefined error codes: `BaseErrorCallbackCode`

| Enum               | code               | Notes                                                          |
|--------------------|--------------------|----------------------------------------------------------------|
| `COMPLETE`         | `200`              | Request succeeded                                              |
| `BAD_REQUEST`      | `BAD_REQUEST`      | Bad request (details masked by default)                        |
| `UNAUTHORIZED`     | `401`              | Not authenticated                                              |
| `ACCESS_FORBIDDEN` | `403`              | Insufficient permission                                        |
| `SERVER_ERROR`     | `SERVER_ERROR`     | Server error (details masked by default)                       |
| `BIZ_EXCEPTION`    | `BIZ_EXCEPTION`    | Generic business exception                                     |
| `ENTRY_NOT_EXISTS` | `ENTRY_NOT_EXISTS` | Data not found (user hint: resource does not exist)            |
| `DUPLICATE_KEY`    | `DUPLICATE_KEY`    | Unique constraint violation (user hint: record already exists) |
| `SIGN_ERROR`       | `SIGN_ERROR`       | Signature verification failed                                  |

#### 3.2.3 Throwing exceptions

```java
// Most common: throw a business exception directly
throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);

// Placeholder formatting
throw new BusinessException(OrderErrorCode.ORDER_OVER_LIMIT, 10);

// With extra data (e.g. return shortage amount when balance is insufficient)
throw new BusinessException(new ShortageInfo(500L), OrderErrorCode.BALANCE_INSUFFICIENT);

// Gateway layer: deliberately mask system exceptions so stack traces don't leak
throw new ServerErrorException(BaseErrorCallbackCode.SERVER_ERROR);
```

Exception semantics:

| Exception class         | Default callback   | `isMaskErrorDetails()` | When to use                                             |
|-------------------------|--------------------|------------------------|---------------------------------------------------------|
| `BusinessException`     | `BIZ_EXCEPTION`    | false                  | Business-rule violation; message shown to user directly |
| `BadRequestException`   | `BAD_REQUEST`      | true                   | Invalid params; production returns a vague hint         |
| `UnauthorizedException` | `UNAUTHORIZED`     | false                  | Token expired / login required                          |
| `AccessDeniedException` | `ACCESS_FORBIDDEN` | false                  | Logged in but not authorized                            |
| `ServerErrorException`  | `SERVER_ERROR`     | true                   | Internal system error; details must be hidden           |

### 3.3 Pagination model

```java
// Request DTO implements PageRequest
@Data
public class UserListQuery implements PageRequest {
    private String keyword;
    private Integer pageNum;     // optional, default 1
    private Integer pageSize;    // optional, default 10
}

// Service-side
int start = query.getStartIndex();   // (pageNum - 1) * pageSize
int end   = query.getEndIndex();

// Return
return new PageResult<>(pageNum, pageSize, total, userList);
```

`PageResult` fields: `pageNum`, `pageSize`, `totalPage`, `total`, `content`.

### 3.4 Signing interface: `BaseSign`

```java
@Data
public class PaymentNotifyRequest implements BaseSign {
    private Long orderId;
    private BigDecimal amount;

    // BaseSign contract
    private String sign;
    private Long nonceTimestamp;
}
```

`SignatureUtil` (in `ddf-common-core`) performs ASCII-sort + HMAC-SHA256 signing over `BaseSign`
DTOs. The server verifies via `SignatureUtil.verify(dto, sign)`, throwing
`BusinessException(SIGN_ERROR)` on mismatch.

### 3.5 Redis key constraint

```java
public enum UserRedisKeyEnum implements RedisKeyConstraint {

    USER_INFO("user:info:%s", Duration.ofHours(1), RedisKeyTypeEnum.STRING, UserInfo.class, null),
    USER_SESSION("user:session:%s", Duration.ofMinutes(30), RedisKeyTypeEnum.STRING, null, null);

    // getKey("1001") → "user:info:1001"
    // getShardingKey("1001") → appends "_shard_N" when a sharding rule is configured
}
```

`RedisKeyConstraint` enforces:

- `template` uses `%s` placeholders
- `getKey(Object...)` auto-validates that argument count matches placeholder count; mismatch throws `ServerErrorException`
- `getShardingKey` appends a shard suffix (e.g. `"_shard_0"`) for multi-Redis-instance routing

### 3.6 Tree structure contract: `ITreeTagCollection`

```java
@Data
public class DeptDTO implements ITreeTagCollection<Long, DeptDTO> {
    private Long treeId;        // department ID
    private Long treeParentId;  // parent department ID
    private Integer sort;       // sort weight
    private List<DeptDTO> children = new ArrayList<>();
}
```

Once the interface is implemented, `TreeConvertUtil.convert(flatList)` in `ddf-common-core`
produces the full tree.

### 3.7 Jackson customizations

- **JSR-310**: `jackson-datatype-jsr310` ensures stable serialization of `LocalDateTime` / `Duration`
- **MsgPack**: `jackson-dataformat-msgpack` supports compact binary serialization, handy for internal RPC
- **`@JsonIgnoreProfile`**: Annotate fields to suppress them under specific profiles (e.g. `prod`),
  used to hide `subMessage` and other debug info in production

### 3.8 Sensitive data masking

```java
@Data
public class UserDTO {
    @SensitiveField(SensitiveStrategy.PHONE)
    private String mobile;

    @SensitiveField(SensitiveStrategy.ID_CARD)
    private String idCard;
}
```

During serialization `13800138000` becomes `138****8000` and `11010119900101****` is masked automatically.

---

## 4. Advanced Usage / Extension Points

### 4.1 Custom global error codes

Create an enum implementing `BaseCallbackCode` in your project's `enums` package:

```java
public enum AppErrorCode implements BaseCallbackCode {
    INVENTORY_SHORTAGE("INV_001", "Inventory insufficient");

    private final String code;
    private final String description;

    @Override public String getCode() { return code; }
    @Override public String getDescription() { return description; }
    @Override public String getBizMessage() { return description; }
}
```

> Error-code strings should be globally unique. Recommended format: `{DOMAIN}_{SEQ}`.

### 4.2 Extending `BaseException`

If the default taxonomy is insufficient (e.g. you need HTTP status mapping or i18n), extend `BaseException`:

```java
public class PaymentException extends BaseException {
    public PaymentException(BaseCallbackCode code) { super(code); }
    @Override public BaseCallbackCode defaultCallback() { return PaymentErrorCode.DEFAULT; }
    @Override public boolean isMaskErrorDetails() { return true; }
}
```

### 4.3 Custom Redis sharding rules

Implement `RedisShardingRule<S, M>` and register it in your `RedisKeyConstraint`:

```java
public class UserIdModSharding implements RedisShardingRule<Long, String> {
    @Override public String getSharding(Long userId) { return "shard_" + (userId % 4); }
}
```

---

## 5. Interplay with Other Modules

| Module                          | How They Cooperate                                                                                                                                              |
|---------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ddf-common-core`               | Upstream dependency; provides default implementations and utilities for `ResponseData`, `BaseException`, `BaseSign`, `RedisKeyConstraint`, `ITreeTagCollection` |
| `ddf-common-mvc`                | Global exception handler maps `BaseException` → `ResponseData`; registers the `@JsonIgnoreProfile` Jackson module                                               |
| `ddf-common-redis`              | All Redis key enums must implement `RedisKeyConstraint`; `ApplicationNamedKeyGenerator` reads `spring.application.name`                                         |
| `ddf-common-authentication`     | Auth exceptions use `UnauthorizedException` / `AccessDeniedException`                                                                                           |
| `ddf-common-limit`              | Rate-limit interceptor returns `ResponseData.failure(...)`                                                                                                      |
| `ddf-common-data-mysql-starter` | `BaseDomain` entity base (in core) works with `PageRequest` / `PageResult` to complete the pagination chain                                                     |

---

## 6. FAQ

**Q1: Why isn't `BaseDomain` in `ddf-common-api`?**  
`BaseDomain` brings in `@CreatedDate` / `@LastModifiedDate` from `spring-data-commons`, which is too
heavy for a pure protocol module. It lives in `ddf-common-core` instead, while `api` keeps
`PageRequest` / `PageResult` which do not depend on Spring Data.

**Q2: What's the difference between `bizMessage` and `description` in `BaseCallbackCode`?**

- `description`: Used during development and debugging; may contain technical details and raw placeholder templates
- `bizMessage`: The final text shown to end users (toast / alert). When `isMaskErrorDetails() = true`
  in production, the system replaces `description` with `bizMessage` to prevent leaking stack traces
  and sensitive information

**Q3: Why does `subMessage` disappear in production?**  
The `subMessage` field carries `@JsonIgnoreProfile(profile = {"pro", "prod"})`. Under the `prod`
profile, Jackson automatically skips it during serialization.

**Q4: Can I use `ddf-common-api` in a non-Spring project?**  
Yes. The only Spring-tight coupling is `spring-boot-starter-validation` (for `@NotNull` etc. on DTOs).
If you don't need validation, you can exclude that dependency and still use `ResponseData`,
`BaseCallbackCode`, `PageResult`, and other pure POJOs.

**Q5: Is there a naming convention for error-code strings?**

- Use UPPER_SNAKE_CASE: `ORDER_NOT_FOUND`, `INVENTORY_SHORTAGE`
- Keep them globally unique (even across services) for easy log grepping and gateway mapping
- Never construct code strings at runtime; all codes must be enum constants

---

## 7. References

- Source: `model/common/response/ResponseData.java`, `model/common/request/PageRequest.java`
- Source: `exception/BaseCallbackCode.java`, `exception/BaseErrorCallbackCode.java`, `exception/BaseException.java`
- Source: `constraint/redis/RedisKeyConstraint.java`, `constraint/collect/ITreeTagCollection.java`
- Source: `model/common/request/BaseSign.java`
- Jackson docs: <https://github.com/FasterXML/jackson-docs/>
