package com.ddf.boot.common.redis.helper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.ddf.boot.common.api.exception.BaseCallbackCode;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.redis.ext.RedisBloomFilter;
import com.ddf.boot.common.redis.request.HashValueUpdateSelectiveCommand;
import com.ddf.boot.common.redis.request.LeakyBucketRateLimitRequest;
import com.ddf.boot.common.redis.request.RateLimitRequest;
import com.ddf.boot.common.redis.request.ZRevRangeBizRankingElementQuery;
import com.ddf.boot.common.redis.request.ZRevRangeBizRankingQuery;
import com.ddf.boot.common.redis.request.ZSetAddDoubleWithMaxCheckCommand;
import com.ddf.boot.common.redis.response.AccessLimitResponse;
import com.ddf.boot.common.redis.response.BatchIncreaseCheckRoundResponse;
import com.ddf.boot.common.redis.response.HashDecreaseUntilFirstLessThanZeroResponse;
import com.ddf.boot.common.redis.response.HashIncrementPersistLimitValueResponse;
import com.ddf.boot.common.redis.response.RankResponse;
import com.ddf.boot.common.redis.response.StringTtlIncrWithLimitResponse;
import com.ddf.boot.common.redis.response.ZRevRangeBizRankingResponse;
import com.ddf.boot.common.redis.response.ZsetZaddWithMaxCheckResponse;
import com.ddf.boot.common.redis.script.RedisLuaScript;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2020/12/11 11:05
 */
@Slf4j
public class RedisTemplateHelper {

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;
    public RedisTemplateHelper(StringRedisTemplate stringRedisTemplate, RedissonClient redissonClient) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
    }

    /**
     * 构造布隆过滤器
     *
     * @param name               名称
     * @param expectedInsertions 预计容器数量
     * @param falseProbability   允许误差率 0~1
     * @param <T> 泛型类型
     * @return
     */
    public <T> RedisBloomFilter<T> createRedisBloomFilter(String name, long expectedInsertions,
        double falseProbability) {
        RBloomFilter<T> bloomFilter = redissonClient.getBloomFilter(name);
        return new RedisBloomFilter<>(name, bloomFilter, expectedInsertions, falseProbability);
    }

    /**
     * 控制某个时间窗口类，对访问总次数进行控制， 如果是偏向流量限流使用的话，应注意时间临界点带来的流量溢出问题， 不建议直接作为限流使用， 更偏向于
     * 业务方面的单位时间逻辑次数控制
     *
     * @param key            目标键
     * @param maxCount       单位时间内最大访问次数
     * @param windowInSecond 窗口时间，单位秒
     * @return
     */
    public AccessLimitResponse sliderWindowAccess(final String key, final long maxCount, final int windowInSecond) {
        final String result = String.valueOf(stringRedisTemplate.execute(
            RedisLuaScript.SLIDER_WINDOW_COUNT, Collections.singletonList(key), String.valueOf(maxCount),
            String.valueOf(TimeUnit.SECONDS.toMillis(windowInSecond)), String.valueOf(System.currentTimeMillis()),
            System.currentTimeMillis() + "-" + IdUtil.randomUUID()
        ));
        return JsonUtil.toBean(result, AccessLimitResponse.class);
    }


    /**
     * 控制某个时间窗口类，对访问总次数进行控制， 如果是偏向流量限流使用的话，应注意时间临界点带来的流量溢出问题， 不建议直接作为限流使用， 更偏向于
     * 业务方面的单位时间逻辑次数控制
     *
     * @param key              目标键
     * @param maxCount         单位时间内最大访问次数
     * @param windowInMillions 窗口时间，单位毫秒
     * @return
     */
    public AccessLimitResponse sliderWindowAccessMillions(final String key, final long maxCount,
        final int windowInMillions) {
        final String result = String.valueOf(stringRedisTemplate.execute(
            RedisLuaScript.SLIDER_WINDOW_COUNT, Collections.singletonList(key), String.valueOf(maxCount),
            String.valueOf(windowInMillions), String.valueOf(System.currentTimeMillis()),
            System.currentTimeMillis() + "-" + IdUtil.randomUUID()
        ));
        return JsonUtil.toBean(result, AccessLimitResponse.class);
    }


    /**
     * 包装{@link RedisTemplateHelper#sliderWindowAccess(String, long, int)}提供一体化的判断，满足条件执行，不满足抛出异常
     *
     * @param key 目标键
     * @param maxCount 最大数量
     * @param windowInSecond windowIN秒参数
     * @param supplier 供应参数
     * @param exceptionCode 异常编码
     * @param <T> 泛型类型
     * @return
     */
    public <T> T sliderWindowAccessCheckException(final String key, final long maxCount, final int windowInSecond,
        Supplier<T> supplier, BaseCallbackCode exceptionCode) {
        final boolean isLimit = sliderWindowAccess(key, maxCount, windowInSecond).isLimited();
        if (isLimit) {
            throw new BusinessException(exceptionCode);
        }
        return supplier.get();
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
     * @param key  目标键
     * @param max  最大值
     * @param rate 每秒钟令牌桶恢复速率
     * @return
     */
    public boolean tokenBucketRateLimitAcquire(String key, Integer max, Integer rate) {
        return tokenBucketRateLimitAcquire(RateLimitRequest
            .builder()
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
     * @param request 请求对象
     */
    public boolean tokenBucketRateLimitAcquire(RateLimitRequest request) {
        final String result = String.valueOf(stringRedisTemplate.execute(
            RedisLuaScript.TOKEN_BUCKET_RATE_LIMIT, Collections.singletonList(request.getKey()),
            String.valueOf(request.getMax()), String.valueOf(request.getRate()),
            String.valueOf(System.currentTimeMillis())
        ));
        return Objects.equals("1", result);
    }

    /**
     * 对String类型的key进行递增递减并设置过期值的原子脚本, 初始值为0， 每次递增+1
     *
     * @param key           目标键
     * @param expireSeconds 过期秒值
     * @return 缓存key对应的最新值
     */
    public Long incrementKeyExpire(String key, long expireSeconds) {
        return Long.parseLong(
            Objects.requireNonNull(stringRedisTemplate.execute(
                RedisLuaScript.STRING_KEY_INCREMENT_EXPIRE, Collections.singletonList(key), "1",
                String.valueOf(expireSeconds)
            )));
    }


    /**
     * 对String类型的key进行递增递减并设置过期指定指定时间的原子脚本
     *
     * @param key      目标键
     * @param expireAt 指定过期的具体时间
     * @return 缓存key对应的最新值
     */
    public Long incrementKeyExpireAt(String key, Date expireAt) {
        if (System.currentTimeMillis() > expireAt.getTime()) {
            throw new IllegalArgumentException("过期时间不能早于当前时间");
        }
        return Long.parseLong(Objects.requireNonNull(stringRedisTemplate.execute(
            RedisLuaScript.STRING_KEY_INCREMENT_EXPIRE_AT, Collections.singletonList(key), "1",
            // 这个单位是秒
            String.valueOf(expireAt.getTime() / 1000)
        )));
    }


    /**
     * 全局分布式限流, 这个看起来就是基于漏桶算法的
     * <p>
     * https://github.com/redisson/redisson/wiki/6.-Distributed-objects
     *
     * @param request 请求对象
     * @return
     */
    public boolean leakyBucketRateLimitAcquire(LeakyBucketRateLimitRequest request) {
        RRateLimiter limiter = redissonClient.getRateLimiter(request.getKey());
        boolean result = limiter.trySetRate(
            RateType.OVERALL, request.getRate(), request.getRateIntervalSeconds(),
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
     * @param key           目标键
     * @param field         字段名
     * @param step          每次自增的值
     * @param limit         自增上限值，超过这个值不会继续自增
     * @param expireSeconds 对key设置最大的过期时间
     * @return
     */
    public AccessLimitResponse hashIncreaseCheck(String key, String field, Long step, Long limit, Long expireSeconds) {
        final String result = stringRedisTemplate.execute(
            RedisLuaScript.HASH_INCREMENT_CHECK,
            Collections.singletonList(key), field, String.valueOf(step), String.valueOf(limit),
            String.valueOf(expireSeconds)
        );
        return JsonUtil.toBean(result, AccessLimitResponse.class);
    }

    /**
     * 基于hash结构的自减并且支持自减下限判定，低于下限，该方法内部提供数据回滚
     *
     * @param key           目标键
     * @param field         字段名
     * @param step          每次自增的值
     * @param limit         自减下限值，低于这个值不会继续自减
     * @param expireSeconds 对key设置最大的过期时间
     * @return
     */
    public AccessLimitResponse hashDecreaseCheck(String key, String field, Long step, Long limit, Long expireSeconds) {
        final String result = stringRedisTemplate.execute(
            RedisLuaScript.HASH_DECREMENT_CHECK,
            Collections.singletonList(key), field, String.valueOf(step), String.valueOf(limit), expireSeconds
        );
        return JsonUtil.toBean(result, AccessLimitResponse.class);
    }


    /**
     * 基于hash结构的自增（正负值）进行上下限判定，如果超出上下限，则将值设置为对应的上下限值
     *
     * @param key           目标键
     * @param field         字段名
     * @param step          每次自增的值
     * @param minValue      小于这个值，则将值设置为这个值
     * @param maxValue      自增上限值，超过这个值不会继续自增
     * @param expireSeconds 对key设置最大的过期时间
     * @return
     * @return
     */
    public HashIncrementPersistLimitValueResponse hashIncrPersistLimitValue(String key, String field, Double step,
        Long minValue, Long maxValue, Long expireSeconds) {
        final String result = stringRedisTemplate.execute(
            RedisLuaScript.HASH_INCREMENT_PERSIST_LIMIT_VALUE, Collections.singletonList(key), field,
            String.valueOf(step), Objects.nonNull(minValue) ? String.valueOf(minValue) : "",
            Objects.nonNull(maxValue) ? String.valueOf(maxValue) : "",
            Objects.nonNull(expireSeconds) ? String.valueOf(expireSeconds) : ""
        );
        return JsonUtil.toBean(result, HashIncrementPersistLimitValueResponse.class);
    }


    /**
     * 基于hash结构的自减并且支持自减下限判定，低于下限，该方法内部提供数据回滚
     *
     * @param key   目标键
     * @param field 字段名
     * @param step  每次自增的值
     * @return
     */
    public HashDecreaseUntilFirstLessThanZeroResponse hashDecreaseUntilFirstLessThanZero(String key, String field,
        Long step) {
        final String result = stringRedisTemplate.execute(
            RedisLuaScript.HASH_DECREASE_UNTIL_FIRST_LESS_THAN_ZERO,
            Collections.singletonList(key), field, String.valueOf(step)
        );
        return JsonUtil.toBean(result, HashDecreaseUntilFirstLessThanZeroResponse.class);
    }

    /**
     * 包装{@link RedisTemplateHelper#stringIncrWithLimit(String, Long, Long, Long)}提供一体化的判断，满足条件执行，不满足抛出异常
     *
     * @param key 目标键
     * @param step step参数
     * @param limit limit参数
     * @param expireSeconds 过期seconds参数
     * @param supplier 供应参数
     * @param exceptionCode 异常编码
     * @param <T> 泛型类型
     * @param field 字段名
     * @return
     */
    public <T> T hashIncrWithLimitCheckException(String key, String field, Long step, Long limit, Long expireSeconds,
        Supplier<T> supplier, BaseCallbackCode exceptionCode) {
        final boolean isLimit = hashIncreaseCheck(key, field, step, limit, expireSeconds).isLimited();
        if (isLimit) {
            throw new BusinessException(exceptionCode);
        }
        return supplier.get();
    }


    /**
     * 基于String结构的自增并且支持自增上限判定，超过上限，该方法内部提供数据回滚
     *
     * @param key           目标键
     * @param step          每次自增的值
     * @param limit         自增上限值，超过这个值不会继续自增
     * @param expireSeconds 对key设置最大的过期时间
     * @return
     */
    public AccessLimitResponse stringIncrWithLimit(String key, Long step, Long limit, Long expireSeconds) {
        final String result = stringRedisTemplate.execute(
            RedisLuaScript.STRING_INCREMENT_CHECK,
            Collections.singletonList(key), String.valueOf(step), String.valueOf(limit), String.valueOf(expireSeconds)
        );
        return JsonUtil.toBean(result, AccessLimitResponse.class);
    }

    /**
     * 包装{@link RedisTemplateHelper#stringIncrWithLimit(String, Long, Long, Long)}提供一体化的判断，满足条件执行，不满足抛出异常
     *
     * @param key 目标键
     * @param step step参数
     * @param limit limit参数
     * @param expireSeconds 过期seconds参数
     * @param supplier 供应参数
     * @param exceptionCode 异常编码
     * @param <T> 泛型类型
     * @return
     */
    public <T> T stringIncrWithLimitCheckException(String key, Long step, Long limit, Long expireSeconds,
        Supplier<T> supplier, BaseCallbackCode exceptionCode) {
        final boolean isLimit = stringIncrWithLimit(key, step, limit, expireSeconds).isLimited();
        if (isLimit) {
            throw new BusinessException(exceptionCode);
        }
        return supplier.get();
    }

    /**
     * 对一个hash结构的hash key的value进行自增取模求整运算，并返回取模后的整数值，运算后取模消耗的值会被减掉
     * 使用场景
     * 比如每次获取3个碎片，当自增到10个碎片后就可以合成一个完整的东西，合成后当前值要减去消耗的数值
     *
     * @param key    目标键
     * @param field  字段名
     * @param step   每次自增的值
     * @param module 模数
     * @return
     */
    public Integer hashIncreaseRoundingReduce(String key, String field, Long step, Long module) {
        final String execute = stringRedisTemplate.execute(
            RedisLuaScript.HASH_INCREASE_ROUNDING_REDUCE,
            Collections.singletonList(key), field, String.valueOf(step), String.valueOf(module)
        );
        return StringUtils.isNotBlank(execute) ? Integer.parseInt(execute) : 0;
    }

    /**
     * 基于zset实现的存储最大历史的容器
     * -- 该脚本的作用是提供一个保留最大长度的容器，如果达到最大容器大小最开始存储的数据会丢失。
     * -- 场景举例，比如保留用户进房历史，最多保留20个。那么member就是主播的id, score就可以用进房时间
     *
     * @param key 目标键
     * @param maxSize 最大大小参数
     * @param member 成员值
     * @param score   分值
     * @return
     */
    public Integer maxCapacityHistoryContainer(String key, Long maxSize, String member, Double score) {
        final String execute = stringRedisTemplate.execute(
            RedisLuaScript.MAX_CAPACITY_HISTORY_CONTAINER,
            Collections.singletonList(key), String.valueOf(maxSize), member, String.valueOf(score)
        );
        return StringUtils.isNotBlank(execute) ? Integer.parseInt(execute) : 0;
    }


    /**
     * 支持根据时间计算小数位完成同score排名的自增，分数相同，完成时间越靠前，生成的小数位越大，从而让积分靠前，注意只支持整数业务
     *
     * @param key 目标键
     * @param score 分值
     * @param member 成员值
     * @return
     */
    public Long zIncrByWithTime(String key, Long score, String member) {
        return zIncrByWithTime(key, score, member, 0L);
    }

    /**
     * 支持根据时间计算小数位完成同score排名的自增，分数相同，完成时间越靠前，生成的小数位越大，从而让积分靠前，注意只支持整数业务
     *
     * @param key 目标键
     * @param score 分值
     * @param member 成员值
     * @param expireSeconds 参数
     * @return
     */
    public Long zIncrByWithTime(String key, Long score, String member, Long expireSeconds) {
        final String execute = stringRedisTemplate.execute(
            RedisLuaScript.ZSET_INCR_WITH_TIME,
            Collections.singletonList(key), member, String.valueOf(score),
            String.valueOf(calcPointScoreByTime(System.currentTimeMillis())), String.valueOf(expireSeconds)
        );
        // 舍弃小数位
        return StringUtils.isNotBlank(execute) ? BigDecimal
            .valueOf(Double.parseDouble(execute))
            .longValue() : 0L;
    }


    /**
     * 针对zIncrByWithTime方法取出榜单数据，在这里处理小数问题
     *
     * @param key 目标键
     * @param start 起始位置
     * @param end 结束位置
     */
    public Set<ZSetOperations.TypedTuple<String>> reverseRangeWithScoresTime(String key, long start, long end) {
        final Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate
            .opsForZSet()
            .reverseRangeWithScores(key, start, end);
        if (Objects.nonNull(tuples) && !tuples.isEmpty()) {
            return tuples
                .stream()
                .map(tuple -> {
                    // 原始分数，包含小数的，需要处理掉
                    final BigDecimal originScore = BigDecimal.valueOf(tuple.getScore());
                    return ZSetOperations.TypedTuple.of(tuple.getValue(), (double) originScore.longValue());
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return tuples;
    }

    /**
     * 支持根据时间计算小数位完成同score排名的自增，分数相同，完成时间越靠前，生成的小数位越大，从而让积分靠前.
     * <p>
     * 由于这种会丢失原始分数的小数，这里提供一个方法，可以将值等比例放大， 比如两位小数，那就对原始积分乘100。
     * 取出来的时候，积分别忘记除回去就行。
     *
     * @param key 目标键
     * @param score 分值
     * @param member 成员值
     * @param enlargeMultiple 参数
     * @return
     */
    public Double zIncrDoubleByWithTime(String key, BigDecimal score, String member, Integer enlargeMultiple) {
        return zIncrDoubleByWithTime(key, score, member, enlargeMultiple, 0L);
    }

    /**
     * 支持根据时间计算小数位完成同score排名的自增，分数相同，完成时间越靠前，生成的小数位越大，从而让积分靠前.
     * <p>
     * 由于这种会丢失原始分数的小数，这里提供一个方法，可以将值等比例放大， 比如两位小数，那就对原始积分乘100。
     * 取出来的时候，积分别忘记除回去就行。
     *
     * @param key 目标键
     * @param score 分值
     * @param member 成员值
     * @param enlargeMultiple 参数
     * @param expireSeconds 参数
     * @return
     */
    public Double zIncrDoubleByWithTime(String key, BigDecimal score, String member, Integer enlargeMultiple,
        Long expireSeconds) {
        if (Objects.isNull(enlargeMultiple) || (enlargeMultiple % 10 != 0 && enlargeMultiple != 1)) {
            throw new IllegalArgumentException("enlargeMultiple must be a multiple of 10 or 1");
        }
        // 反算出小数位
        final int scale = enlargeMultiple == 1 ? 0 : enlargeMultiple / 10;

        // 计算放大后的 score 和当前时间的分数
        BigDecimal logicScore;
        if (enlargeMultiple == 1) {
            // 当 enlargeMultiple 为 1 时，不放大分数
            logicScore = score;
        } else {
            // 放大分数
            logicScore = score.multiply(BigDecimal.valueOf(enlargeMultiple));
        }
        final String execute = stringRedisTemplate.execute(
            RedisLuaScript.ZSET_INCR_WITH_TIME,
            Collections.singletonList(key), member, logicScore,
            String.valueOf(calcPointScoreByTime(System.currentTimeMillis())), String.valueOf(expireSeconds)
        );

        // 原始分数，包含小数的，需要处理掉
        final BigDecimal originScore = BigDecimal.valueOf(Double.parseDouble(execute));

        // 如果 enlargeMultiple 为 1，直接返回原始分数
        if (enlargeMultiple == 1) {
            return originScore.doubleValue();
        }

        // 还原成原始分数，逻辑处理
        return originScore
            .divide(BigDecimal.valueOf(enlargeMultiple), scale, RoundingMode.DOWN)
            .doubleValue();
    }


    /**
     * 针对zIncrDoubleByWithTime方法取出榜单数据，由于同时支持小数和按时间排序，因此数值被放大存储，这里要还原回真实分数
     *
     * @param key 目标键
     * @param start 起始位置
     * @param end 结束位置
     * @param enlargeMultiple 参数
     */
    public Set<ZSetOperations.TypedTuple<String>> reverseRangeDoubleWithScoresTime(String key, long start, long end,
        Integer enlargeMultiple) {
        if (Objects.isNull(enlargeMultiple) || (enlargeMultiple % 10 != 0 && enlargeMultiple != 1)) {
            throw new IllegalArgumentException("enlargeMultiple must be a multiple of 10 or 1");
        }
        final Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate
            .opsForZSet()
            .reverseRangeWithScores(key, start, end);
        // 反算出小数位
        // 反算出小数位
        final int scale = enlargeMultiple == 1 ? 0 : enlargeMultiple / 10;
        if (Objects.nonNull(tuples) && !tuples.isEmpty()) {
            return tuples
                .stream()
                .map(tuple -> {
                    // 原始分数， 本身有小数被放大存储，这里还原成原始分数，逻辑处理。
                    final BigDecimal originScore = BigDecimal.valueOf(tuple.getScore());
                    if (enlargeMultiple == 1) {
                        // 放大一倍的即默认是整数， 直接取整返回了
                        return ZSetOperations.TypedTuple.of(tuple.getValue(), (double) originScore.longValue());
                    }
                    // 否则反算回去返回真实逻辑double
                    return ZSetOperations.TypedTuple.of(
                        tuple.getValue(), originScore
                            .divide(BigDecimal.valueOf(enlargeMultiple), scale, RoundingMode.DOWN)
                            .doubleValue()
                    );
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return tuples;
    }

    /**
     * 取出目标元素的排名, 翻译成人类语言，排名从1开始
     *
     * @param key 目标键
     * @param element element参数
     * @return
     */
    public Long rankByElement(String key, String element) {
        final Long rank = stringRedisTemplate
            .opsForZSet()
            .reverseRank(key, element);
        if (Objects.isNull(rank)) {
            return 0L;
        }
        return rank + 1;
    }


    /**
     * 查询指定元素前后榜单数据
     *
     * @param key 目标键
     * @param element element参数
     * @param beforeFetchSize beforefetch大小参数
     * @param afterFetchSize afterfetch大小参数
     * @param enlargeMultiple 参数
     * @return
     */
    public List<RankResponse> rankAround(String key, String element, Integer beforeFetchSize, Integer afterFetchSize,
        Integer enlargeMultiple) {
        if (Objects.isNull(enlargeMultiple) || (enlargeMultiple % 10 != 0 && enlargeMultiple != 1)) {
            throw new IllegalArgumentException("enlargeMultiple must be a multiple of 10 or 1");
        }
        // 反算出小数位
        final int scale = enlargeMultiple == 1 ? 0 : enlargeMultiple / 10;
        final String execute = stringRedisTemplate.execute(
            RedisLuaScript.ZSET_AROUND_ELEMENT_RANK,
            Collections.singletonList(key), element, beforeFetchSize.toString(), afterFetchSize.toString()
        );
        final List<RankResponse> list = JsonUtil.toList(execute, RankResponse.class);
        // 如果 enlargeMultiple 为 1，直接返回原始分数
        if (enlargeMultiple == 1) {
            return list;
        }
        for (RankResponse response : list) {
            // 原始分数， 本身有小数被放大存储，这里还原成原始分数，逻辑处理。
            final BigDecimal originScore = BigDecimal.valueOf(response.getScore());
            // 还原成原始分数，逻辑处理
            response.setScore(originScore
                .divide(BigDecimal.valueOf(enlargeMultiple), scale, RoundingMode.DOWN)
                .doubleValue());
        }

        return list;
    }


    /**
     * 查询指定元素前后榜单数据， 有并发问题，留着备用看下以前逻辑
     *
     * @param key 目标键
     * @param element element参数
     * @param beforeFetchSize beforefetch大小参数
     * @param afterFetchSize afterfetch大小参数
     * @return
     */
    @Deprecated
    public List<RankResponse> rankAround2BackUp(String key, String element, Integer beforeFetchSize,
        Integer afterFetchSize) {
        final Long rank = rankByElement(key, element);
        if (rank == 0L) {
            return new ArrayList<>();
        }
        // 因为取的rank是从1开始的，转回 0-based
        long zeroBasedRank = rank - 1;
        final long beforeIndex = Math.max(0, zeroBasedRank - beforeFetchSize);
        final long endIndex = zeroBasedRank + afterFetchSize;
        final Set<ZSetOperations.TypedTuple<String>> tuples = reverseRangeWithScoresTime(key, beforeIndex, endIndex);
        if (CollUtil.isEmpty(tuples)) {
            log.error(
                "rankAround error, key: {}, element: {}, beforeFetchSize: {}, afterFetchSize: {}", key, element,
                beforeFetchSize, afterFetchSize
            );
            return new ArrayList<>();
        }
        final List<RankResponse> collect = tuples
            .stream()
            .map(t -> RankResponse.of(t.getValue(), t.getScore(), 0L))
            .collect(Collectors.toList());
        if (collect
            .stream()
            .noneMatch(r -> r
                .getElement()
                .equals(element))) {
            log.error(
                "rankAround error, key: {}, element: {}, beforeFetchSize: {}, afterFetchSize: {}, collect= {}",
                key, element, beforeFetchSize, afterFetchSize, JsonUtil.toJson(collect)
            );
        }
        buildRank(collect, element, rank);
        return collect;
    }


    /**
     * 基于string实现的对一个key进行ttl续期操作，用来实现某些倒计时，又可以增加倒计时的场景
     * 场景1：
     * 1. 送礼物会增加热度值， 热度值每秒会下降一次，最少为0.当热度值达到100时，则触发一个幸运时刻，但是继续从100开始倒计时，
     * 只不过因为达到了最大值，这个时候的倒计时就是幸运时刻的倒计时，然后再次送礼物增加的热度值就会延长幸运时刻的时间。
     * 相当于同一个倒计时，根据是否达到最大值来判定两种状态，是未达到条件的倒计时还是已经达到条件的倒计时。
     *
     * @param key 目标键
     * @param incrTtl incrTTL参数
     * @param maxTtl 最大TTL参数
     * @return
     */
    public StringTtlIncrWithLimitResponse stringTtlIncrWithLimit(String key, Integer incrTtl, Integer maxTtl) {
        final String execute = stringRedisTemplate.execute(
            RedisLuaScript.STRING_TTL_INCR_WITH_LIMIT,
            Collections.singletonList(key), String.valueOf(incrTtl), String.valueOf(maxTtl)
        );
        final StringTtlIncrWithLimitResponse response = new StringTtlIncrWithLimitResponse();
        response.setTtl(0);
        response.setFull(false);
        if (StringUtils.isBlank(execute)) {
            return response;
        }
        final String[] split = execute.split("-");
        response.setFull(Objects.equals("1", split[0]));
        response.setTtl(Integer.parseInt(split[1]));
        return response;
    }


    /**
     * 基于Hash对field进行value的判断， 如果为预期值则删除，否则不删除
     *
     * @param key 目标键
     * @param field 字段名
     * @param checkValue check值
     * @return
     */
    public Integer hashDeleteWithCheckValue(String key, String field, String checkValue) {
        final String execute = stringRedisTemplate.execute(
            RedisLuaScript.HASH_DELETE_WITH_CHECK_VALUE,
            Collections.singletonList(key), field, checkValue
        );
        return StringUtils.isNotBlank(execute) ? Integer.parseInt(execute) : 0;
    }

    /**
     * 基于String进行value的判断， 如果为预期值则删除，否则不删除
     *
     * @param key 目标键
     * @param checkValue check值
     * @return
     */
    public Integer stringDeleteWithCheckValue(String key, String checkValue) {
        final String execute = stringRedisTemplate.execute(
            RedisLuaScript.STRING_DELETE_WITH_CHECK_VALUE,
            Collections.singletonList(key), checkValue
        );
        return StringUtils.isNotBlank(execute) ? Integer.parseInt(execute) : 0;
    }


    /**
     * 对hash的field进行incr操作， 当key是第一次操作时，设置过期时间，后续不会设置过期时间
     *
     * @param key 目标键
     * @param field 字段名
     * @param step step参数
     * @param ttlSeconds TTLseconds参数
     * @return
     */
    public Long hashIncrWithFirstSetTtl(String key, String field, Long step, Long ttlSeconds) {
        final String execute = stringRedisTemplate.execute(
            RedisLuaScript.HASH_INCR_WITH_FIRST_SET_TTL,
            Collections.singletonList(key), field, step, ttlSeconds
        );
        return Long.parseLong(execute);
    }


    /**
     * 该脚本的作用类似于对集合进行最大值判断，当达到最后值后，将组成当前最大值的所有子元素以及对应的数量返回
     *
     * @param maxElementKey 当前集合元素数量的key, string结构
     * @param elementKey    存储子元素的key， hash结构， hash key为identity
     * @param completeSeq   每次集合数量满一次，这个数量便+1
     * @param identity      elementKey的field
     * @param increaseValue 本次增加的数量
     * @param maxValue      最大允许的数量
     * @return
     */
    public Map<String, String> maxElementDict(String maxElementKey, String elementKey, String completeSeq,
        String identity, Long increaseValue, Long maxValue) {
        final String execute = stringRedisTemplate.execute(
            RedisLuaScript.MAX_ELEMENT_DICT,
            Lists.newArrayList(maxElementKey, elementKey, completeSeq), identity, increaseValue, maxValue
        );
        if (StringUtils.isBlank(execute)) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<>();
        final String[] teamList = execute.split(";");
        for (String s : teamList) {
            if (StringUtils.isBlank(s)) {
                continue;
            }
            final String[] split = s.split(":");
            map.put(split[0], split[1]);
        }
        return map;
    }
    /**
     * @param key 目标键
     * @param elementKey 参数
     * @param step 参数
     */
    public Long hashIncrFloatRoundDecimal(String key, String elementKey, Double step) {
        final String execute = stringRedisTemplate.execute(
            RedisLuaScript.HASH_INCR_FLOAT_ROUND_DECIMAL,
            Lists.newArrayList(key), elementKey, step.toString()
        );
        if (Objects.isNull(execute)) {
            return 0L;
        }
        return Long.parseLong(execute);
    }

    /**
     * 范围查询并删除
     *
     * @param key 目标键
     * @param min 最小值
     * @param max 最大值
     * @return
     */
    public List<String> zsetRangeByscoreZrem(String key, Long min, Long max) {
        final String execute = stringRedisTemplate.execute(
            RedisLuaScript.ZSET_RANGEBYSCORE_ZREM,
            Lists.newArrayList(key), min.toString(), max.toString()
        );
        if (StringUtils.isBlank(execute) || "{}".equals(execute)) {
            return new ArrayList<>();
        }
        return JsonUtil.toBean(execute, List.class, String.class);
    }


    /**
     * 基于hash结构的自增并且支持自增上限判定，超过上限，该方法内部提供数据回滚
     *
     * @param key           目标键
     * @param fields        字段集合
     * @param stepList      每次自增的值
     * @param limit         自增上限值，超过这个值不会继续自增
     * @param expireSeconds 对key设置最大的过期时间
     * @return
     */
    public BatchIncreaseCheckRoundResponse batchHashIncreaseCheck(String key, List<String> fields, List<Long> stepList,
        List<Long> limit, Long expireSeconds) {
        final String result = stringRedisTemplate.execute(
            RedisLuaScript.HASH_BATCH_INCREMENT_CHECK, Collections.singletonList(key), String.join(",", fields),
            stepList
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")), limit
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")), String.valueOf(expireSeconds)
        );
        return JsonUtil.toBean(result, BatchIncreaseCheckRoundResponse.class);
    }


    /**
     * hash结构的结构，对多个redis key 进行固定的field自增并且支持自增上限判定，超过上限，该方法内部提供所有Redis key数据回滚
     *
     * @param keys          键集合
     * @param field         字段名
     * @param stepList      每次自增的值
     * @param limit         自增上限值，超过这个值不会继续自增
     * @param expireSeconds 对key设置最大的过期时间
     * @return
     */
    public BatchIncreaseCheckRoundResponse multipleHashBatchHashIncreaseCheck(List<String> keys, String field,
        List<Long> stepList, List<Long> limit, Long expireSeconds) {
        final String result = stringRedisTemplate.execute(
            RedisLuaScript.MULTIPLE_HASH_BATCH_INCREMENT_CHECK, keys, field, stepList
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")), limit
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")), String.valueOf(expireSeconds)
        );
        return JsonUtil.toBean(result, BatchIncreaseCheckRoundResponse.class);
    }

    /**
     * 基于zset实现的zadd操作，当score大于已有值时才会更新, 支持小数，因为要同时支持相同分数，先完成的在前面，使用小数设计的，有冲突，因此
     * 该方法内部将小数放大成整数存储，取出来用的时候要注意
     *
     * @param request 请求对象
     * @return
     */
    public ZsetZaddWithMaxCheckResponse zSetAddWithMaxCheckSupportBiz(ZSetAddDoubleWithMaxCheckCommand request) {
        final Integer scoreFactory = request.getScoreFactory();
        if (Objects.isNull(scoreFactory) || (scoreFactory != 1 && scoreFactory % 10 != 0)) {
            throw new IllegalArgumentException("scoreFactory must be a multiple of 10 or 1");
        }
        final String execute = stringRedisTemplate.execute(
            RedisLuaScript.ZSET_ZADD_WITH_TIME_MAX_CHECK,
            Lists.newArrayList(request.getRankingKey(), request.getDetailKey(), request.getSumKey()),
            request.getElement(), request
                .getScore()
                .toString(), String.valueOf(request.getScoreFactory()), String.valueOf(request.getScoreDecimal()),
            String.valueOf(request.getExpireSeconds()),
            StringUtils.isNotBlank(request.getDetailJson()) ? request.getDetailJson() : ""
        );
        return JsonUtil.toBean(execute, ZsetZaddWithMaxCheckResponse.class);
    }

    /**
     * 业务榜单查询，支持各种汇总查询
     * <p>
     * 本质是是对{@link RedisTemplateHelper#zSetAddWithMaxCheckSupportBiz(ZSetAddDoubleWithMaxCheckCommand)}的查询
     * <p>
     * 发现个离谱的事情， 如果startIdx和endIdx都设置为0，脚本会报错，识别到unknown redis command, 但直接使用cli运行命令就没问题，不知道是不是
     * redisson解析参数有bug
     * 通过wireshake抓包发现发送的数据包里的keys数量参数变成了2，第三个参数被吃掉了
     * EVAL "local rankingKey = KEYS[1] local detailKey = KEYS[2] local sumKey = KEYS[3] local startIdx = tonumber(ARGV[1]) local endIdx = tonumber(ARGV[2]) local scoreFactor = tonumber(ARGV[3]) local totalElements = redis.call('ZCARD', rankingKey) local totalScore = 0 if sumKey and sumKey ~= '' then local s = redis.call('GET', sumKey) totalScore = s and tonumber(s) or 0 end local rawList = redis.call('ZREVRANGE', rankingKey, startIdx, endIdx, 'WITHSCORES') local resultList = {} if cjson.empty_array_mt then setmetatable(resultList, cjson.empty_array_mt) end local rank = startIdx; for i = 1, #rawList, 2 do rank = rank + 1 local element = rawList[i] local fs = tonumber(rawList[i + 1]) local realScore = math.floor(fs / scoreFactor) local detail if detailKey and detailKey ~= '' then detail = redis.call('HGET', detailKey, element) else detail = nil end table.insert(resultList, { element = element, score = realScore, rank = (rank), detail = detail }) end local result = { totalElements = totalElements, totalScore = totalScore, list = resultList } local jsonResult = cjson.encode(result) if not cjson.empty_array_mt then jsonResult = string.gsub(jsonResult, '\"list\":%s*{}', '\"list\":[]') end return jsonResult" 3 game:{fixed_ranking}:crash_ranking:max_multiple:forever_max game:{fixed_ranking}:crash_ranking:max_multiple:DETAIL:forever_max game:{fixed_ranking}:crash_ranking:max_multiple:SUM:forever_max 0 0 1
     *
     * @param query 查询参数
     */
    public ZRevRangeBizRankingResponse zSetRevRangeBizRankingQuery(ZRevRangeBizRankingQuery query) {
        final Integer scoreFactory = query.getScoreFactory();
        if (Objects.isNull(scoreFactory) || (scoreFactory != 1 && scoreFactory % 10 != 0)) {
            throw new IllegalArgumentException("scoreFactory must be a multiple of 10 or 1");
        }
        final String execute = stringRedisTemplate.execute(
            RedisLuaScript.ZSET_REV_RANGE_BIZ_RANKING_QUERY,
            Lists.newArrayList(query.getRankingKey(), query.getDetailKey(), query.getSumKey()),
            String.valueOf(query.getStartIndex()), String.valueOf(query.getEndIndex()),
            String.valueOf(query.getScoreFactory())
        );
        return JsonUtil.toBean(execute, ZRevRangeBizRankingResponse.class);
    }
    /**
     * @param time 参数
     */
    public static BigDecimal calcPointScoreByTime(long time) {
        //        final BigDecimal decimal = new BigDecimal(time * Math.pow(
        //            10, Math.negateExact(String
        //                .valueOf(time)
        //                .length())
        //        ));

        BigDecimal t = new BigDecimal(time);
        BigDecimal pow = BigDecimal.TEN.pow(String
            .valueOf(time)
            .length());
        return BigDecimal.ONE.subtract(t.divide(pow, 20, RoundingMode.DOWN));
    }


    /**
     * 处理排名
     *
     * @param rankResponses rankresponses参数
     * @param element element参数
     * @param elementRank elementrank参数
     */
    private void buildRank(List<RankResponse> rankResponses, String element, Long elementRank) {
        int elementIndex = 0;
        for (int i = 0; i < rankResponses.size(); i++) {
            final RankResponse loop = rankResponses.get(i);
            if (Objects.equals(element, loop.getElement())) {
                elementIndex = i;
                break;
            }
        }

        for (int i = 0; i < rankResponses.size(); i++) {
            final RankResponse loop = rankResponses.get(i);
            loop.setRank(elementRank + (i - elementIndex));
        }
    }

    /**
     * 基于hash实现的对指定元素的字段进行选择性更新, value 必须是一个大对象json，否则没必要使用这个脚本
     *
     * @param command command参数
     */
    public String hashValueUpdateSelective(HashValueUpdateSelectiveCommand command) {
        return stringRedisTemplate.execute(
            RedisLuaScript.HASH_VALUE_UPDATE_SELECTIVE, Collections.singletonList(command.getKey()), command.getField(),
            command.getValue()
        );
    }

    /**
     * 对榜单的单个元素进行多维度数据获取查询
     * @param query 参数
     */
    public ZRevRangeBizRankingResponse.Element zSetRevRangeBizRankingQueryElement(
        ZRevRangeBizRankingElementQuery query) {
        final Integer scoreFactory = query.getScoreFactory();
        if (Objects.isNull(scoreFactory) || (scoreFactory != 1 && scoreFactory % 10 != 0)) {
            throw new IllegalArgumentException("scoreFactory must be a multiple of 10 or 1");
        }
        final String execute = stringRedisTemplate.execute(
            RedisLuaScript.ZSET_REV_RANGE_USER_BIZ_RANKING_ELEMENT_QUERY,
            Lists.newArrayList(query.getRankingKey(), query.getDetailKey()), query.getElement(),
            String.valueOf(query.getScoreFactory())
        );
        return JsonUtil.toBean(execute, ZRevRangeBizRankingResponse.Element.class);
    }

    /**
     * 多维度业务榜单榜单回滚, 如果积分匹配的话，则执行回滚
     * @param command 参数
     */
    public ZsetZaddWithMaxCheckResponse zSetDeleteWithMaxScoreCheck(ZSetAddDoubleWithMaxCheckCommand command) {
        final Integer scoreFactory = command.getScoreFactory();
        if (Objects.isNull(scoreFactory) || (scoreFactory != 1 && scoreFactory % 10 != 0)) {
            throw new IllegalArgumentException("scoreFactory must be a multiple of 10 or 1");
        }
        final String execute = stringRedisTemplate.execute(
            RedisLuaScript.ZSET_DELETE_WITH_MAX_SCORE_CHECK,
            Lists.newArrayList(command.getRankingKey(), command.getDetailKey(), command.getSumKey()),
            command.getElement(), command
                .getScore()
                .toString(), String.valueOf(command.getScoreFactory()), String.valueOf(command.getScoreDecimal()),
            String.valueOf(command.getExpireSeconds())
        );
        return JsonUtil.toBean(execute, ZsetZaddWithMaxCheckResponse.class);
    }
}
