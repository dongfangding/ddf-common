package com.ddf.boot.common.limit.ratelimit.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.api.util.UserContextUtil;
import com.ddf.boot.common.limit.exception.LimitExceptionCode;
import com.ddf.boot.common.limit.ratelimit.annotation.MultiRateLimit;
import com.ddf.boot.common.limit.ratelimit.annotation.RateLimit;
import com.ddf.boot.common.limit.ratelimit.annotation.RateLimitIgnore;
import com.ddf.boot.common.limit.ratelimit.config.RateLimitProperties;
import com.ddf.boot.common.limit.ratelimit.extra.RateLimitPropertiesCollect;
import com.ddf.boot.common.limit.ratelimit.keygenerator.RateLimitKeyGenerator;
import com.ddf.boot.common.mvc.util.AopUtil;
import com.ddf.boot.common.redis.helper.RedisTemplateHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * <p>限流处理器</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2021/02/24 11:58
 */
@Aspect
@Slf4j
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisTemplateHelper redisTemplateHelper;
    private final RateLimitProperties rateLimitProperties;
    private final ObjectProvider<RateLimitPropertiesCollect> rateLimitPropertiesCollect;
    private final Map<String, RateLimitKeyGenerator> keyGeneratorMap;

    public static final String BEAN_NAME = "rateLimitAspect";

    private final StandardReflectionParameterNameDiscoverer discoverer =
            new StandardReflectionParameterNameDiscoverer();

    private final ExpressionParser parser = new SpelExpressionParser();

    @Pointcut(value = "@annotation(com.ddf.boot.common.limit.ratelimit.annotation.RateLimit)"
            + " || @within(com.ddf.boot.common.limit.ratelimit.annotation.RateLimit)"
            + " || @annotation(com.ddf.boot.common.limit.ratelimit.annotation.MultiRateLimit)")
    public void pointCut() {

    }

    /**
     * 处理限流逻辑
     *
     * @param joinPoint joinpoint参数
     * @throws NoSuchMethodException
     */
    @Before(value = "pointCut()")
    public void before(JoinPoint joinPoint) throws NoSuchMethodException {
        // 获取当前拦截类
        final Class<?> currentClass = joinPoint
                .getSignature()
                .getDeclaringType();
        // 获取当前拦截方法
        MethodSignature currentMethod = (MethodSignature) joinPoint.getSignature();
        if (currentMethod
                .getMethod()
                .isAnnotationPresent(RateLimitIgnore.class)) {
            log.info(
                    "忽略执行[{}]-[{}]的限流处理>>>>>>>>>>>>>>>>>>>>>>", currentClass.getName(),
                    currentMethod.getName()
            );
            return;
        }
        final MultiRateLimit multiRateLimit = AopUtil.getAnnotation(joinPoint, MultiRateLimit.class);
        // 限流规则，统一用集合处理
        final List<RateLimit> rules;
        if (Objects.nonNull(multiRateLimit)) {
            rules = new ArrayList<>(Arrays.asList(multiRateLimit.rules()));
        } else {
            // 获取限流注解
            final RateLimit annotation = AopUtil.getAnnotation(joinPoint, RateLimit.class);
            if (Objects.isNull(annotation)) {
                log.debug(
                        "[{}-{}]未开启限流限流>>>>>>>>>>>>>>>>>>>>>>", currentClass.getName(), currentMethod.getName());
                return;
            }
            rules = Collections.singletonList(annotation);
        }
        if (CollectionUtil.isEmpty(rules)) {
            log.debug("[{}-{}]限流规则为空>>>>>>>>>>>>>>>>>>>>>>", currentClass.getName(), currentMethod.getName());
            return;
        }

        // 处理扩展接口，可使用外部特性时时刷新属性，如使用Spring-Cloud的配置时时刷新特性
        if (rateLimitProperties.isCloudRefresh()) {
            final RateLimitPropertiesCollect propertiesCollectIfAvailable =
                    rateLimitPropertiesCollect.getIfAvailable();
            if (Objects.isNull(propertiesCollectIfAvailable)) {
                throw new NoSuchBeanDefinitionException(
                        "当使用了cloudRefresh=true时， 请务必同时实现接口[%s]".formatted(
                                RateLimitPropertiesCollect.class.getName()));
            }
            propertiesCollectIfAvailable.copyToProperties(rateLimitProperties);
        }
        // 属性检查（全局属性，循环外执行一次即可）
        rateLimitProperties.check();
		// 身份标识 这里如果用户不存在，但是是c端应用的话，可能会有设备号或者之类的标识客户端的唯一身份的，如果有，最好使用这个
        String identityNo = StringUtils.defaultIfBlank(UserContextUtil.getUserId(), UserContextUtil.getImei());

        // 允许多个限流规则存在，如接口全局限流以及也同时需要控制用户级别的防刷
        for (RateLimit annotation : rules) {
            // 获取限流最大令牌桶数量
            Integer max = annotation.max();
            if (Objects.equals(RateLimitProperties.NOT_CONTROL, max) || !condition(joinPoint, annotation, currentMethod)) {
                continue;
            }
            // 获取key生成器
            final String keyGenerator = StringUtils.isBlank(annotation.keyGenerator()) ?
                    rateLimitProperties.getKeyGenerator() : annotation.keyGenerator();
            if (StringUtils.isBlank(keyGenerator)) {
                return;
            }

            // 获取令牌恢复速率
			Integer rate = annotation.rate() == rateLimitProperties.getRate() ? rateLimitProperties.getRate() :
					annotation.rate();
            if (Objects.equals(RateLimitProperties.NOT_CONTROL, rate)) {
                return;
            }

            // 强制性校验，避免隐藏错误
            if (!keyGeneratorMap.containsKey(keyGenerator)) {
                throw new NoSuchBeanDefinitionException("限流组件[%s]不存在".formatted(keyGenerator));
            }

            // 生成限流的key
            String key = keyGeneratorMap
                    .get(keyGenerator)
                    .generateKey(joinPoint, annotation, rateLimitProperties);
            if (!redisTemplateHelper.tokenBucketRateLimitAcquire(key, max, rate)) {
                log.error(
                        "接口【{}-{}-{}】超过限流组件{}预定流量，过滤请求， 完整key规则为: {}, 对应参数{}, 记录日志>>>>>>>",
                        identityNo, currentClass.getName(), currentMethod.getName(), keyGenerator, key,
                        AopUtil.serializeParam(joinPoint)
                );
                throw new BusinessException(LimitExceptionCode.RATE_LIMIT);
            }
        }
    }

    /**
     * 处理条件表达式, 满足条件的才会限流
     *
     * @param joinPoint joinpoint参数
     * @param annotation annotation参数
     * @param currentMethod currentmethod参数
     * @return 返回条件表达式是否满足
     */
    private boolean condition(JoinPoint joinPoint, RateLimit annotation, MethodSignature currentMethod) {
        if (StringUtils.isBlank(annotation.condition())) {
            return true;
        }
        String[] params = discoverer.getParameterNames(currentMethod.getMethod());
        StandardEvaluationContext context = new StandardEvaluationContext();

        if (Objects.nonNull(params)) {
            for (int i = 0; i < joinPoint.getArgs().length; i++) {
                context.setVariable(params[i], joinPoint.getArgs()[i]);
            }
            Expression expression = parser.parseExpression(annotation.condition());
            final Object value = expression.getValue(context);
            if (value instanceof Boolean boolean1) {
                return boolean1;
            }
        }
        return true;
    }
}
