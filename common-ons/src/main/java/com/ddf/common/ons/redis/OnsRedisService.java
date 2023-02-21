package com.ddf.common.ons.redis;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/**
 * ONS消费幂等性保证实现
 *
 * @author steveguo
 * @date 2020-08-20 3:11 PM
 */
@Component(value = OnsRedisService.BEAN_NAME)
public class OnsRedisService {

    public static final String BEAN_NAME = "onsRedisService";

    @Resource(name = "onsRedisTemplate")
    private StringRedisTemplate onsRedisTemplate;

    public Long incr(String key) {
        return onsRedisTemplate.opsForValue().increment(key, 1);
    }

    public Long increment(String key, Long value) {
        return onsRedisTemplate.opsForValue().increment(key,value);
    }

    public void expire(String key, long timeout, TimeUnit unit) {
        onsRedisTemplate.expire(key, timeout, unit);
    }

    public Object get(String key) {
        return onsRedisTemplate.opsForValue().get(key);
    }

    public void set(String key, String value) {
        onsRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, Long timeout) {
        onsRedisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    public void delete(String key) {
        onsRedisTemplate.delete(key);
    }

    public Long sAdd(String key, String member) {
        return onsRedisTemplate.opsForSet().add(key, member);
    }

    public Boolean sIsMember(String key, String member) {
        return onsRedisTemplate.opsForSet().isMember(key, member);
    }

    public boolean setBit(String key, int offset, boolean value) {
        return onsRedisTemplate.opsForValue().setBit(key, offset, value);
    }

    public boolean getBit(String key, int offset) {
        return onsRedisTemplate.opsForValue().getBit(key, offset);
    }

    public boolean expire(String key, long timeout) {
        return onsRedisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    public Boolean sIsMemberByPipelined(String key, String member) {
        List<Object> objects = onsRedisTemplate.executePipelined((RedisCallback<Object>) connection ->
                connection.sIsMember(key.getBytes(), member.getBytes()));
        return null != objects && objects.size() > 0 ? Boolean.parseBoolean(objects.get(0).toString()) : false;
    }

    public Long sAddAndExpireByPipelined(String key, String member, long timeout) {
        List<Object> objects = onsRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            Long result = connection.sAdd(key.getBytes(), member.getBytes());
            connection.expire(key.getBytes(), timeout);
            return result;
        });
        return null != objects && objects.size() > 0 ? Long.parseLong(objects.get(0).toString()) : 0L;
    }

    public Long incrAndExpireByPipelined(String key, long timeout) {
        List<Object> objects = onsRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            Long result = connection.incr(key.getBytes());
            connection.expire(key.getBytes(), timeout);
            return result;
        });
        return null != objects && objects.size() > 0 ? Long.parseLong(objects.get(0).toString()) : 0L;
    }

    public Long incrByAndExpireByPipelined(String key, Long value, long timeout) {
        List<Object> objects = onsRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            Long result = connection.incrBy(key.getBytes(), value);
            connection.expire(key.getBytes(), timeout);
            return result;
        });
        return null != objects && objects.size() > 0 ? Long.parseLong(objects.get(0).toString()) : 0L;
    }

    private static final String sAddAndExpireScript = "local result = redis.call('sadd', KEYS[1], ARGV[1]) if result > 0 then redis.call('expire', KEYS[1], ARGV[2]) end return result ";
    private static final String incrAndExpireScript = "local result = redis.call('incr', KEYS[1]) if result > 0 then redis.call('expire', KEYS[1], ARGV[1]) end return result  ";

    private DefaultRedisScript<Long> sAddAndExpireRedisScript;
    private DefaultRedisScript<Long> incrAndExpireRedisScript;

    @PostConstruct
    public void loadLuaScript(){
        sAddAndExpireRedisScript = new DefaultRedisScript<>();
        sAddAndExpireRedisScript.setScriptText(sAddAndExpireScript);
        sAddAndExpireRedisScript.setResultType(Long.class);

        incrAndExpireRedisScript = new DefaultRedisScript<>();
        incrAndExpireRedisScript.setScriptText(incrAndExpireScript);
        incrAndExpireRedisScript.setResultType(Long.class);

    }

    
    public Long sAddAndExpire(String key, String member, long timeout) {
        return  onsRedisTemplate.execute(sAddAndExpireRedisScript, Collections.singletonList(key), member, String.valueOf(timeout));
    }

    
    public Long incrAndExpire(String key, long timeout) {
        return  onsRedisTemplate.execute(incrAndExpireRedisScript, Collections.singletonList(key), String.valueOf(timeout));
    }

    public Long generateIdempotentOffset(String key, String field) {
        HashOperations<String, Object, Object> hashOperations = onsRedisTemplate.opsForHash();
        Object value = hashOperations.get(key, field);
        if(Objects.nonNull(value)) {
            return Long.valueOf(value.toString());
        }
        Long maxValue = hashOperations.values(key).stream()
                .map(Object::toString)
                .map(Long::valueOf)
                .reduce(Long::max)
                .orElse(20L); // 保证不会跟之前的重复
        Long newValue = maxValue + BigDecimal.ONE.longValue();
        if(hashOperations.putIfAbsent(key, field, newValue.toString())) {
            return newValue;
        }
        return generateIdempotentOffset(key, field);
    }

    public Long hdel(String key, String field) {
        return onsRedisTemplate.opsForHash().delete(key, field);
    }
    
    public Long srem(String key, String member) {
        return onsRedisTemplate.opsForSet().remove(key, member);
    }

    public Boolean setIfAbsent(String key, String value, Long timeSeconds) {
        return onsRedisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(timeSeconds));
    }

}
