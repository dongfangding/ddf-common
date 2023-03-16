package com.ddf.boot.common.redis.helper;

import cn.hutool.core.util.IdUtil;
import com.ddf.boot.common.api.exception.BaseCallbackCode;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.redis.request.LeakyBucketRateLimitRequest;
import com.ddf.boot.common.redis.request.RateLimitRequest;
import com.ddf.boot.common.redis.response.HashIncrementCheckResponse;
import com.ddf.boot.common.redis.script.RedisLuaScript;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/12/11 11:05
 */
public class RedisTemplateHelper {

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    public RedisTemplateHelper(StringRedisTemplate stringRedisTemplate, RedissonClient redissonClient) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
    }

    /**
     * 控制某个时间窗口类，对访问总次数进行控制， 如果是偏向流量限流使用的话，应注意时间临界点带来的流量溢出问题， 不建议直接作为限流使用， 更偏向于
     * 业务方面的单位时间逻辑次数控制
     *
     * @param key            缓存key
     * @param maxCount       单位时间内最大访问次数
     * @param windowInSecond 窗口时间，单位秒
     * @return
     */
    public boolean sliderWindowAccess(final String key, final long maxCount, final int windowInSecond) {
        final String result = String.valueOf(
                stringRedisTemplate.execute(
                        RedisLuaScript.SLIDER_WINDOW_COUNT, Collections.singletonList(key),
                        String.valueOf(maxCount), String.valueOf(windowInSecond),
                        String.valueOf(System.currentTimeMillis()),
                        System.currentTimeMillis() + "-" + IdUtil.randomUUID()
                ));
        return Objects.equals("1", result);
    }

    /**
     * 控制某个时间窗口类，对访问总次数进行控制， 如果是偏向流量限流使用的话，应注意时间临界点带来的流量溢出问题， 不建议直接作为限流使用， 更偏向于
     * 业务方面的单位时间逻辑次数控制
     *
     * @param key            缓存key
     * @param maxCount       单位时间内最大访问次数
     * @param expiredAt      过期的具体时间点
     * @return
     */
    public boolean sliderWindowAccessExpiredAt(final String key, final long maxCount, final Date expiredAt) {
        Long expiredSeconds = 0L;
        Date now = new Date();
        if (expiredAt.compareTo(now) > 0) {
            expiredSeconds = (expiredAt.getTime() / 1000) - (now.getTime() / 1000);
        }
        final String result = String.valueOf(
                stringRedisTemplate.execute(
                        RedisLuaScript.SLIDER_WINDOW_COUNT, Collections.singletonList(key),
                        String.valueOf(maxCount), String.valueOf(expiredSeconds),
                        String.valueOf(System.currentTimeMillis()),
                        System.currentTimeMillis() + "-" + IdUtil.randomUUID()
                ));
        return Objects.equals("1", result);
    }

    /**
     * 提供一体化的判断，满足条件执行，不满足抛出异常
     *
     * @param key
     * @param maxCount
     * @param windowInSecond
     * @param supplier
     * @param exceptionCode
     * @param <T>
     * @return
     */
    public <T> T sliderWindowAccessCheckException(final String key, final long maxCount, final int windowInSecond, Supplier<T> supplier, BaseCallbackCode exceptionCode) {
        final boolean b = sliderWindowAccess(key, maxCount, windowInSecond);
        if (b) {
            return supplier.get();
        }
        throw new BusinessException(exceptionCode);
    }

    /**
     * 提供一体化的判断，满足条件执行，不满足抛出异常
     *
     * @param key
     * @param maxCount
     * @param expiredAt
     * @param supplier
     * @param exceptionCode
     * @param <T>
     * @return
     */
    public <T> T sliderWindowAccessExpiredAtCheckException(final String key, final long maxCount, final Date expiredAt, Supplier<T> supplier, BaseCallbackCode exceptionCode) {
        final boolean b = sliderWindowAccessExpiredAt(key, maxCount, expiredAt);
        if (b) {
            return supplier.get();
        }
        throw new BusinessException(exceptionCode);
    }

    /**
     * 全局分布式限流, 基于令牌桶算法
     * <p>
     * 底层使用hash实现， 使用ttl实现单位毫秒内的key过期， 格式内容如下， 具体实现逻辑可进入脚本查看
     * <p>
     * 一个hash结构的key内部对象类两个hash key参数
     * "last_time":"1607769790054",
     * "current_token": "0"
     * <p>
     * last_time 上次恢复令牌时间
     * current_token为剩余的token，注意这个数量有可能不是最新的， 因为要在获取的时间才会按照恢复速率重新计算剩余令牌数
     *
     * @param key  缓存key
     * @param max  单位时间内最大令牌桶数量
     * @param rate 每秒钟令牌桶恢复速率
     * @return
     */
    public boolean tokenBucketRateLimitAcquire(String key, Integer max, Integer rate) {
        return tokenBucketRateLimitAcquire(RateLimitRequest.builder()
                .key(key)
                .max(max)
                .rate(rate)
                .ignorePrefix(true)
                .build());
    }

    /**
     * 全局分布式限流, 基于令牌桶算法
     * <p>
     * 底层使用hash实现， 使用ttl实现单位秒内的key过期， 格式内容如下
     * <p>
     * 一个hash结构的key内部对象类两个hash key参数
     * "last_time":"1607769790054",
     * "current_token": "0"
     * <p>
     * current_token为剩余的token，
     *
     * @param request
     */
    public boolean tokenBucketRateLimitAcquire(RateLimitRequest request) {
        final String result = String.valueOf(stringRedisTemplate.execute(
                RedisLuaScript.TOKEN_BUCKET_RATE_LIMIT,
                Collections.singletonList(request.getKey()), String.valueOf(request.getMax()),
                String.valueOf(request.getRate()), String.valueOf(System.currentTimeMillis())
        ));
        return Objects.equals("1", result);
    }

    /**
     * 对String类型的key进行递增递减并设置过期值的原子脚本, 初始值为0， 每次递增+1
     *
     * @param key           key
     * @param expireSeconds 过期秒值
     * @return 缓存key对应的最新值
     */
    public Long incrementKeyExpire(String key, long expireSeconds) {
        return Long.parseLong(Objects.requireNonNull(
                stringRedisTemplate.execute(RedisLuaScript.STRING_KEY_INCREMENT_EXPIRE, Collections.singletonList(key),
                        "1", String.valueOf(expireSeconds)
                )));
    }


    /**
     * 对String类型的key进行递增递减并设置过期指定指定时间的原子脚本
     *
     * @param key      key
     * @param expireAt 指定过期的具体时间
     * @return 缓存key对应的最新值
     */
    public Long incrementKeyExpireAt(String key, Date expireAt) {
        if (System.currentTimeMillis() > expireAt.getTime()) {
            throw new IllegalArgumentException("过期时间不能早于当前时间");
        }
        return Long.parseLong(Objects.requireNonNull(
                stringRedisTemplate.execute(RedisLuaScript.STRING_KEY_INCREMENT_EXPIRE_AT,
                        Collections.singletonList(key), "1",
                        // 这个单位是秒
                        String.valueOf(expireAt.getTime() / 1000)
                )));
    }


    /**
     * 全局分布式限流, 这个看起来就是基于漏桶算法的
     * <p>
     * https://github.com/redisson/redisson/wiki/6.-Distributed-objects
     *
     * @param request
     * @return
     */
    public boolean leakyBucketRateLimitAcquire(LeakyBucketRateLimitRequest request) {
        RRateLimiter limiter = redissonClient.getRateLimiter(request.getKey());
        boolean result = limiter.trySetRate(RateType.OVERALL, request.getRate(), request.getRateIntervalSeconds(),
                RateIntervalUnit.SECONDS
        );
        if (!Objects.equals(Boolean.TRUE, result)) {
            return Boolean.FALSE;
        }
        return limiter.tryAcquire();
    }


    /**
     * 基于hash结构的自增并且支持自增上限判定，超过上限，该方法内部提供数据回滚
     *
     * @param key     要操作的key
     * @param hashKey 要操作的hash key
     * @param step    每次自增的值
     * @param limit   自增上限值，超过这个值不会继续自增
     * @return
     */
    public HashIncrementCheckResponse hashIncrAndCheck(String key, String hashKey, Long step, Long limit) {
        final long result = Long.parseLong(Objects.requireNonNull(
                stringRedisTemplate.execute(RedisLuaScript.HASH_INCREMENT_CHECK, Collections.singletonList(key),
                        hashKey, String.valueOf(step), String.valueOf(limit), String.valueOf(TimeUnit.DAYS.toSeconds(1))
                )));
        return HashIncrementCheckResponse.builder()
                .result(result)
                .actualResult(result > limit ? (result - step) : result)
                .build();
    }

    /**
     * 对一个hash结构的hash key的value进行自增取模求整运算，并返回取模后的整数值，运算后取模消耗的值会被减掉
     * 使用场景
     * 比如每次获取3个碎片，当自增到10个碎片后就可以合成一个完整的东西，合成后当前值要减去消耗的数值
     *
     *
     * @param key     要操作的key
     * @param hashKey 要操作的hash key
     * @param step    每次自增的值
     * @param module  模数
     * @return
     */
    public Integer hashIncreaseRoundingReduce(String key, String hashKey, Long step, Long module) {
        final String execute = stringRedisTemplate.execute(RedisLuaScript.HASH_INCREASE_ROUNDING_REDUCE,
                Collections.singletonList(key), hashKey, step + "", module + "");
        return StringUtils.isNotBlank(execute) ? Integer.parseInt(execute) : 0;
    }

    /**
     * 基于zset实现的存储最大历史的容器
     * -- 该脚本的作用是提供一个保留最大长度的容器，如果达到最大容器大小最开始存储的数据会丢失。
     * -- 场景举例，比如保留用户进房历史，最多保留20个。那么member就是主播的id, score就可以用进房时间
     *
     * @param key
     * @param maxSize
     * @param member
     * @param score
     * @return
     */
    public Integer maxCapacityHistoryContainer(String key, Long maxSize, String member, Double score) {
        final String execute = stringRedisTemplate.execute(RedisLuaScript.MAX_CAPACITY_HISTORY_CONTAINER,
                Collections.singletonList(key), maxSize + "", member, score + "");
        return StringUtils.isNotBlank(execute) ? Integer.parseInt(execute) : 0;
    }
}
