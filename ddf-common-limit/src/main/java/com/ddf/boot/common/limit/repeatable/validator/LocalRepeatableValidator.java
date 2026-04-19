package com.ddf.boot.common.limit.repeatable.validator;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.StrUtil;
import com.ddf.boot.common.limit.repeatable.annotation.Repeatable;
import com.ddf.boot.common.limit.repeatable.config.RepeatableProperties;
import com.ddf.boot.common.mvc.util.AopUtil;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;

/**
 * <p>基于本地缓存实现的防重提交验证器</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2021/02/05 11:43
 */
@Slf4j
public class LocalRepeatableValidator implements RepeatableValidator {

    /**
     * 该类beanName
     */
    public static final String BEAN_NAME = "localRepeatableValidator";

    /**
     * 基于弱引用，并限制最大缓存时间, 这种基于本地缓存的，无法支持集群处理
     */
    private final TimedCache<String, RequestValue> requestMap = CacheUtil.newWeakCache(TimeUnit.MINUTES.toMillis(10));

    /**
     * 执行表单放重校验逻辑
     *
     * @param joinPoint  织入点
     * @param repeatable 注解
     * @param currentUid 参数
     * @param repeatableProperties 参数
     * @return 是否通过校验
     */
    @Override
    public boolean check(JoinPoint joinPoint, Repeatable repeatable, String currentUid, RepeatableProperties repeatableProperties) {
        // 获取定义的间隔时间
        final long interval = repeatable.interval() == 0 ? repeatableProperties.getInterval() : repeatable.interval();
        final long currentTimeMillis = System.currentTimeMillis();

        // 获取缓存key
        final String key = getRequestMapKey(joinPoint, currentUid);
        // 获取当前请求value对象
        final RequestValue currentValue = getRequestMapValue(joinPoint);
        final RequestValue cacheValue = requestMap.get(key);
        if (cacheValue == null) {
            requestMap.put(key, currentValue);
            return true;
        }

        // 执行校验逻辑
        if (Objects.equals(currentValue.getValue(), cacheValue.getValue())
                && currentTimeMillis - cacheValue.getCurrentTime() < interval) {
            return false;
        }
        requestMap.put(key, currentValue);
        return true;
    }


    /**
     * 获取缓存key
     *
     * @param joinPoint 参数
     * @param currentUid 参数
     * @return
     */
    private String getRequestMapKey(JoinPoint joinPoint, String currentUid) {
        return StrUtil.join(":", currentUid,
                AopUtil.getJoinPointClass(joinPoint).getName(),
                AopUtil.getJoinPointMethod(joinPoint).getName());
    }

    /**
     * 获取缓存的value对象
     *
     * @param jointPoint 参数
     * @return
     */
    private RequestValue getRequestMapValue(JoinPoint jointPoint) {
        final RequestValue requestValue = new RequestValue();
        requestValue.setValue(AopUtil.serializeParam(jointPoint));
        requestValue.setCurrentTime(System.currentTimeMillis());
        return requestValue;
    }



    /**
     * 缓存的value, 基于请求参数和时间
     */
    @Data
    public static class RequestValue {

        /**
         * 请求参数value值
         */
        private String value;

        /**
         * 填入缓存的时间
         */
        private long currentTime;

    }
}