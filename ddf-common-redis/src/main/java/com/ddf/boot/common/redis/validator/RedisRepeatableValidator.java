package com.ddf.boot.common.redis.validator;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.ddf.boot.common.core.repeatable.Repeatable;
import com.ddf.boot.common.core.repeatable.RepeatableValidator;
import com.ddf.boot.common.core.util.AopUtil;
import com.ddf.boot.common.core.util.JsonUtil;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * <p>基于redis实现防重复提交校验器</p >
 *
 * 使用了最简单的方式，直接根据请求按照规则生成key并设置过期时间， 如果下次请求相同的key存在，则校验不通过
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/02/05 14:18
 */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class RedisRepeatableValidator implements RepeatableValidator {

    private StringRedisTemplate stringRedisTemplate;

    /**
     * 验证器bean_name
     */
    public static final String BEAN_NAME = "redisRepeatableValidator";

    /**
     * redis 校验器key的前缀
     */
    public static final String KEY_PREFIX = "repeatable";

    /**
     * 固定value
     */
    public static final String FIXED_VALUE = "1";

    /**
     * 执行表单放重校验逻辑
     *
     * @param joinPoint  织入点
     * @param repeatable 注解
     * @param currentUid 用户uid
     * @return 是否通过校验
     */
    @Override
    public boolean check(JoinPoint joinPoint, Repeatable repeatable, String currentUid) {
        // 获取当前拦截类
        final Class<?> currentClass = joinPoint.getSignature().getDeclaringType();
        // 获取当前拦截方法
        MethodSignature currentMethod = (MethodSignature) joinPoint.getSignature();
        // 获取定义的间隔时间
        final long interval = repeatable.interval();

        String paramValue = JsonUtil.asString(AopUtil.getParamMap(joinPoint));
        // 使用用户uid做盐值
        HMac mac = new HMac(HmacAlgorithm.HmacMD5, currentUid.getBytes(StandardCharsets.UTF_8));
        // 生成key规则
        String redisKey = StrUtil.join(":", KEY_PREFIX, currentUid, currentClass.getName(),
                currentMethod.getName(), mac.digestHex(paramValue));
        final ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        // 执行校验逻辑，key存在则校验不通过，不存在，则存入key
        final Boolean bool = operations.setIfAbsent(redisKey, FIXED_VALUE, interval, TimeUnit.MILLISECONDS);
        if (Objects.isNull(bool)) {
            return true;
        }
        return bool;
    }
}
