package com.ddf.boot.common.alarm.enums;



import com.ddf.boot.common.api.constraint.redis.RedisKeyConstraint;
import com.ddf.boot.common.api.constraint.redis.RedisShardingRule;
import com.ddf.boot.common.api.enums.RedisKeyTypeEnum;
import java.time.Duration;

/**
 * <p>redis key 定义枚举</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2022/05/21 20:36
 */
public enum AlarmRedisKeyEnum implements RedisKeyConstraint {

    /**
     * 钉钉每日发送限制
     * %s yyyyMMdd
     */
    DING_TALK_DAILY_LIMIT("limit:alarm:ding_talk:%s", Duration.ofDays(2), RedisKeyTypeEnum.STRING),


    ;
    /**
     * key模板，变量使用%s代替
     * 如sms_code:%s:%s
     */
    private final String template;

    /**
     * 过期秒数,这里不会根据这个做什么事情，自己定义自己使用就行，这里主要是一些固定业务使用的key过期时间是固定的，就在这里当常量定义了
     * 如短信验证码，需要的是一个常量的过期时间，那就在这里定义，用的时候引用这里就行，其它情况下意义不大
     */
    private Duration ttl;

    /**
     * value对应的对象class
     */
    private Class clazz;

    private RedisKeyTypeEnum keyType;

    /**
     * key的分片规则
     */
    private RedisShardingRule redisShardingRule;

    AlarmRedisKeyEnum(String template, RedisKeyTypeEnum keyType) {
        this.template = template;
        this.ttl = Duration.ofSeconds(-1);
        this.keyType = keyType;
    }

    AlarmRedisKeyEnum(String template, RedisKeyTypeEnum keyType, Class clazz) {
        this.template = template;
        this.ttl = Duration.ofSeconds(-1);
        this.keyType = keyType;
        this.clazz = clazz;
    }

    AlarmRedisKeyEnum(String template, Duration ttl, RedisKeyTypeEnum keyType) {
        this.template = template;
        this.ttl = ttl;
        this.keyType = keyType;
    }

    AlarmRedisKeyEnum(String template, Duration ttl, RedisKeyTypeEnum keyType, Class clazz) {
        this.template = template;
        this.ttl = ttl;
        this.keyType = keyType;
        this.clazz = clazz;
    }

    @Override
    public String getTemplate() {
        return template;
    }

    @Override
    public Duration getTtl() {
        return ttl;
    }

    @Override
    public RedisKeyTypeEnum getRedisKeyType() {
        return keyType;
    }

    @Override
    public Class getClazz() {
        return clazz;
    }

    @Override
    public RedisShardingRule getRedisShardingRule() {
        return redisShardingRule;
    }
}
