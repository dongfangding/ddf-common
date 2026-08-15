package com.ddf.boot.common.limit.repeatable.validator;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.ddf.boot.common.limit.repeatable.annotation.Repeatable;
import com.ddf.boot.common.limit.repeatable.config.RepeatableProperties;
import com.ddf.boot.common.mvc.util.AopUtil;
import com.ddf.boot.common.redis.constant.ApplicationNamedKeyGenerator;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * <p>基于redis实现防重复提交校验器</p >
 * 使用了最简单的方式，直接根据请求按照规则生成key并设置过期时间， 如果下次请求相同的key存在，则校验不通过
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2021/02/05 14:18
 */
@Slf4j
@RequiredArgsConstructor
public class RedisRepeatableValidator implements RepeatableValidator {

    private final StringRedisTemplate stringRedisTemplate;

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
    private static final String FIXED_VALUE = "1";

    /**
     * 执行表单放重校验逻辑
     *
     * @param joinPoint 织入点
     * @param repeatable 注解
     * @param currentUid 用户uid
     * @return 是否通过校验
     */
    @Override
    public boolean check(JoinPoint joinPoint, Repeatable repeatable, String currentUid,
            RepeatableProperties repeatableProperties) {
        // 获取定义的间隔时间
        final long interval = repeatable.interval() == 0 ? repeatableProperties.getInterval() : repeatable.interval();

        // 匿名且无设备号时身份标识为空，使用固定值兜底，避免 NPE 并保证仍能基于参数做防重
        final String uid = StringUtils.defaultIfBlank(currentUid, "anonymous");

        String paramValue = AopUtil.serializeParam(joinPoint);
        // 使用用户uid做盐值
        HMac mac = new HMac(HmacAlgorithm.HmacMD5, uid.getBytes(StandardCharsets.UTF_8));
        // 生成key规则
        String redisKey = ApplicationNamedKeyGenerator.genKey(KEY_PREFIX, uid,
                AopUtil.getJoinPointClass(joinPoint).getName(), AopUtil.getJoinPointMethod(joinPoint).getName(),
                mac.digestHex(paramValue));
        final ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        // 执行校验逻辑，key存在则校验不通过，不存在，则存入key。
        // setIfAbsent 返回 true 表示首次设置成功（放行），false 表示已存在（重复提交），
        // null 表示 Redis 连接异常，此时不应失败开放，按重复提交拒绝处理，避免绕过防重。
        final Boolean bool = operations.setIfAbsent(redisKey, FIXED_VALUE, interval, TimeUnit.MILLISECONDS);
        return Boolean.TRUE.equals(bool);
    }
}
