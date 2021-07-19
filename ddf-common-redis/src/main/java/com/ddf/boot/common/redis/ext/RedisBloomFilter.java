package com.ddf.boot.common.redis.ext;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;

/**
 * 基于Redis实现布隆过滤器{@link RBloomFilter}
 * <p>
 * 亦可参考guava基于JVM实现版本 {@link com.google.common.hash.BloomFilter}
 * <p>
 * 布隆过滤器特点：
 * 1> 当它说某个值不存在时，那就肯定不存在。
 * 2> 当它说某个值存在时，这个值不一定存在。(即不确定)
 * <p>
 * 使用场景：
 * 1> 防止缓存雪崩；将数据库中所有的查询条件，放入布隆过滤器中。当一个查询请求过来时，先经过布隆过滤器进行查，如果判断请求查询值存在，则继续查；如果判断请求查询不存在，直接丢弃。
 * 2> 批量发送消息；将发送的消息标识身份key，放入布隆过滤器中。再次触发发送时，检查是否存在布隆过滤器中，若不存在，则可认定为没有发送过，此时可发送消息。
 * <p>
 * 注意：
 * 需要留意布隆起是否需要初始化。
 * 即将相应业务数据需要初始化到布隆过滤器中。
 * 否则"特点1"不成立，会造成误判。{@link #contains(Object)}
 *
 * @param <T>
 * @author Mitchell
 * @version 1.0
 * @date 2020/09/18 13:42
 */
@Slf4j
public class RedisBloomFilter<T> {

    private final String name;

    private final RBloomFilter<T> bloomFilter;

    /**
     * 构造布隆过滤器
     *
     * @param name               redis key name
     * @param redissonClient     redisson client
     * @param expectedInsertions 预计容器数量
     * @param falseProbability   允许误差率 0~1
     * @param <T>
     * @return
     */
    public static <T> RedisBloomFilter<T> newInstance(String name, RedissonClient redissonClient,
            long expectedInsertions, double falseProbability) {
        RBloomFilter<T> bloomFilter = redissonClient.getBloomFilter(name);
        return new RedisBloomFilter<>(name, bloomFilter, expectedInsertions, falseProbability);
    }

    private RedisBloomFilter(String name, RBloomFilter<T> bloomFilter, long expectedInsertions,
            double falseProbability) {
        this.name = name;
        this.bloomFilter = bloomFilter;
        bloomFilter.tryInit(expectedInsertions, falseProbability);
    }

    /**
     * 判断是否包含指定元素
     *
     * @param object
     * @return
     */
    public boolean contains(T object) {
        boolean result = bloomFilter.contains(object);
        log.info("{}[{}] contains object={}, result={}", this.getClass().getSimpleName(), name, object, result);
        return result;
    }


    /**
     * 添加元素
     *
     * @param object
     * @return
     */
    public boolean add(T object) {
        boolean result = bloomFilter.add(object);
        log.info("{}[{}] add object={}, result={}", this.getClass().getSimpleName(), name, object, result);
        return result;
    }

}
