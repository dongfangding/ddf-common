package com.ddf.boot.common.redis.script;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2021/01/16 15:53
 */
public interface RedisLuaScript {

    /**
     * 基于令牌桶算法的分布式限流
     */
    RedisScript<String> TOKEN_BUCKET_RATE_LIMIT = RedisScript.of(new ClassPathResource("lua/tokenBucketRateLimit.lua"),
            String.class);

    /**
     * 对String类型的key进行递增递减并设置剩余过期时间的原子脚本
     */
    RedisScript<String> STRING_KEY_INCREMENT_EXPIRE = RedisScript.of(
            new ClassPathResource("lua/stringIncrementExpire.lua"), String.class);

    /**
     * 对String类型的key进行递增递减并设置过期指定指定时间的原子脚本
     */
    RedisScript<String> STRING_KEY_INCREMENT_EXPIRE_AT = RedisScript.of(
            new ClassPathResource("lua/stringIncrementExpireAt.lua"), String.class);

    /**
     * 基于滑动窗口，对指定时间段的访问总数进行控制
     */
    RedisScript<String> SLIDER_WINDOW_COUNT = RedisScript.of(new ClassPathResource("lua/sliderWindowCount.lua"),
            String.class);

    /**
     * 基于hash结构的自增并增加上限判定的通用脚本
     */
    RedisScript<String> HASH_INCREMENT_CHECK = RedisScript.of(new ClassPathResource("lua/HashIncreaseCheck.lua"),
            String.class);

    /**
     * 基于hash结构的自减并增加下限判定的通用脚本
     */
    RedisScript<String> HASH_DECREMENT_CHECK = RedisScript.of(new ClassPathResource("lua/HashDecreaseCheck.lua"),
            String.class);


    /**
     * 基于hash结构的自增，但是当达到上限时，会将值设置为上限值，而不是回滚本次增加的值
     */
    RedisScript<String> HASH_INCREMENT_PERSIST_LIMIT_VALUE = RedisScript.of(
            new ClassPathResource("lua/HashIncreasePersistLimitValue.lua"), String.class);

    /**
     * 基于String结构的自增并增加上限判定的通用脚本
     */
    RedisScript<String> STRING_INCREMENT_CHECK = RedisScript.of(
            new ClassPathResource("lua/StringIncrementWithLimitCheck.lua"), String.class);

    /**
     * hash自增时进行取模运算后反减运算消耗值，结果返回取整后的值
     */
    RedisScript<String> HASH_INCREASE_ROUNDING_REDUCE = RedisScript.of(
            new ClassPathResource("lua/HashIncreaseRoundingReduce.lua"), String.class);

    /**
     * 基于zset实现的存储最大历史的容器
     */
    RedisScript<String> MAX_CAPACITY_HISTORY_CONTAINER = RedisScript.of(
            new ClassPathResource("lua/MaxCapacityHistoryContainer.lua"), String.class);

    /**
     * 基于zset实现整数位带时间戳的
     */
    RedisScript<String> ZSET_INCR_WITH_TIME = RedisScript.of(new ClassPathResource("lua/ZsetIncrWithTime.lua"),
            String.class);

    /**
     * 基于String实现对一个key进行ttl带有上限值的累加操作，用来实现某些倒计时，又可以增加倒计时的场景
     */
    RedisScript<String> STRING_TTL_INCR_WITH_LIMIT = RedisScript.of(new ClassPathResource("lua/TtlIncrWithLimit.lua"),
            String.class);

    /**
     * 基于Hash对field进行value的判断， 如果为预期值则删除，否则不删除
     */
    RedisScript<String> HASH_DELETE_WITH_CHECK_VALUE = RedisScript.of(
            new ClassPathResource("lua/HashDeleteWithCheckValue.lua"), String.class);

    /**
     * 该脚本的作用是在hash递减时进行下限判定，如果小于下限则回退本次减少数值， 脚本提供递减和判断以及回退的整个原子性保证
     */
    RedisScript<String> HASH_DECREASE_UNTIL_FIRST_LESS_THAN_ZERO = RedisScript.of(
            new ClassPathResource("lua/HashDecreaseUntilFirstLessThanZero.lua"), String.class);

    /**
     * 基于String进行value的判断， 如果为预期值则删除，否则不删除
     */
    RedisScript<String> STRING_DELETE_WITH_CHECK_VALUE = RedisScript.of(
            new ClassPathResource("lua/StringDeleteWithCheckValue.lua"), String.class);

    /**
     * 该脚本的作用是对hash的field进行incr操作， 当key是第一次操作时，设置过期时间，后续不会设置过期时间
     */
    RedisScript<String> HASH_INCR_WITH_FIRST_SET_TTL = RedisScript.of(
            new ClassPathResource("lua/HashIncrWithFirstSetTtl.lua"), String.class);

    /**
     * 该脚本的作用类似于对集合进行最大值判断，当达到最后值后，将组成当前最大值的所有子元素以及对应的数量返回
     */
    RedisScript<String> MAX_ELEMENT_DICT = RedisScript.of(new ClassPathResource("lua/MaxElementDict.lua"),
            String.class);

    /**
     * 小数位递增，计算出整数位，返回整数位，并且存储的值减去整数位
     */
    RedisScript<String> HASH_INCR_FLOAT_ROUND_DECIMAL = RedisScript.of(
            new ClassPathResource("lua/HashIncrFloatRoundDecimal.lua"), String.class);


    /**
     * 基于zset实现的范围删除
     */
    RedisScript<String> ZSET_RANGEBYSCORE_ZREM = RedisScript.of(new ClassPathResource("lua/zset_rangebyscore_zrem.lua"),
            String.class);


    /**
     * 基于hash结构的批量自增并增加上限判定的通用脚本, 对同一个key的多个field进行自增判断,
     */
    RedisScript<String> HASH_BATCH_INCREMENT_CHECK = RedisScript.of(
            new ClassPathResource("lua/HashBatchIncreaseCheck.lua"), String.class);


    /**
     * 基于hash结构的批量自增并增加上限判定的通用脚本, 对同一个key的多个field进行自增判断,
     */
    RedisScript<String> MULTIPLE_HASH_BATCH_INCREMENT_CHECK = RedisScript.of(
            new ClassPathResource("lua/MultipleHashIncreaseCheck.lua"), String.class);

    /**
     * 基于zset实现的zadd操作，当score大于已有值时才会更新, 注意这个是带时间戳检查的，小数位不是真实的时间戳
     */
    RedisScript<String> ZSET_ZADD_WITH_TIME_MAX_CHECK = RedisScript.of(
            new ClassPathResource("lua/zadd_with__max_score_check.lua"), String.class);


    /**
     * 基于zset实现的zadd操作，当score大于已有值时才会更新, 注意这个是带时间戳检查的，小数位不是真实的时间戳
     */
    RedisScript<String> ZSET_ZADD_WITH_MAX_CHECK = RedisScript.of(new ClassPathResource("lua/zadd_with_max_check.lua"),
            String.class);

    /**
     * 基于zset实现的对指定元素的前后元素进行查询
     */
    RedisScript<String> ZSET_AROUND_ELEMENT_RANK = RedisScript.of(
            new ClassPathResource("lua/zset_around_element_rank.lua"), String.class);

    /**
     * 基于hash实现的对指定元素的字段进行选择性更新
     */
    RedisScript<String> HASH_VALUE_UPDATE_SELECTIVE = RedisScript.of(
            new ClassPathResource("lua/HashValueUpdateSelective.lua"), String.class);


    /**
     * 基于hash实现的对指定元素的字段进行选择性更新
     */
    RedisScript<String> ZSET_REV_RANGE_BIZ_RANKING_QUERY = RedisScript.of(
            new ClassPathResource("lua/zrevrange_biz_ranking_query.lua"), String.class);

    /**
     * 对榜单进行多维度数据获取查询
     */
    RedisScript<String> ZSET_REV_RANGE_USER_BIZ_RANKING_ELEMENT_QUERY = RedisScript.of(
            new ClassPathResource("lua/zrevrange_biz_ranking_element_query.lua"), String.class);

    /**
     * 多维度业务榜单榜单回滚
     */
    RedisScript<String> ZSET_DELETE_WITH_MAX_SCORE_CHECK = RedisScript.of(
            new ClassPathResource("lua/zdelete_with_max_score_check.lua"), String.class);
}
