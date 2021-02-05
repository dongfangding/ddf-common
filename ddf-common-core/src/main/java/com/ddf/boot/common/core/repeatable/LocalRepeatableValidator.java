package com.ddf.boot.common.core.repeatable;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.StrUtil;
import com.ddf.boot.common.core.util.AopUtil;
import com.ddf.boot.common.core.util.JsonUtil;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * <p>基于本地缓存实现的防重提交验证器</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/02/05 11:43
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
     * @return 是否通过校验
     */
    @Override
    public boolean check(JoinPoint joinPoint, Repeatable repeatable, String currentUid) {
        // 获取定义的间隔时间
        final long interval = repeatable.interval();
        final long currentTimeMillis = System.currentTimeMillis();

        // 获取缓存key
        final String key = getRequestMapKey(joinPoint, currentUid);
        // 获取当前请求value对象
        final RequestValue currentValue = getRequestMapValue(joinPoint);
        if (!requestMap.containsKey(key)) {
            requestMap.put(key, currentValue);
            return true;
        }

        // 获取缓存的value
        final RequestValue cacheValue = requestMap.get(key);

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
     * @return
     */
    private String getRequestMapKey(JoinPoint joinPoint, String currentUid) {
        // 获取当前拦截类
        final Class<?> currentClass = joinPoint.getSignature().getDeclaringType();
        // 获取当前拦截方法
        MethodSignature currentMethod = (MethodSignature) joinPoint.getSignature();
        return StrUtil.join(":", currentUid, currentClass.getName(), currentMethod.getName());
    }

    /**
     * 获取缓存的value对象
     *
     * @return
     */
    private RequestValue getRequestMapValue(JoinPoint jointPoint) {
        final RequestValue requestValue = new RequestValue();
        requestValue.setValue(JsonUtil.asString(AopUtil.getParamMap(jointPoint)));
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
