package com.ddf.boot.common.limit.ratelimit.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.ddf.boot.common.authentication.util.UserContextUtil;
import com.ddf.boot.common.core.exception200.BusinessException;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.ddf.boot.common.core.util.AopUtil;
import com.ddf.boot.common.limit.exception.LimitExceptionCode;
import com.ddf.boot.common.limit.ratelimit.annotation.MultiRateLimit;
import com.ddf.boot.common.limit.ratelimit.annotation.RateLimit;
import com.ddf.boot.common.limit.ratelimit.annotation.RateLimitIgnore;
import com.ddf.boot.common.limit.ratelimit.config.RateLimitProperties;
import com.ddf.boot.common.limit.ratelimit.extra.RateLimitPropertiesCollect;
import com.ddf.boot.common.limit.ratelimit.keygenerator.RateLimitKeyGenerator;
import com.ddf.boot.common.redis.helper.RedisTemplateHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * <p>限流处理器</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/24 11:58
 */
@Aspect
@Slf4j
public class RateLimitAspect {

    @Autowired
    private RedisTemplateHelper redisTemplateHelper;
    @Autowired
    private RateLimitProperties rateLimitProperties;
    @Autowired(required = false)
    private RateLimitPropertiesCollect rateLimitPropertiesCollect;

    public static final String BEAN_NAME = "rateLimitAspect";

    private final LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * key生成规则实现器
     *
     */
    public static final Map<String, RateLimitKeyGenerator> KEY_GENERATOR_MAP = SpringContextHolder
            .getBeansOfType(RateLimitKeyGenerator.class);

    @Pointcut(value = "@annotation(com.ddf.boot.common.limit.ratelimit.annotation.RateLimit)"
            + " || @within(com.ddf.boot.common.limit.ratelimit.annotation.RateLimit)"
            + " || @annotation(com.ddf.boot.common.limit.ratelimit.annotation.MultiRateLimit)")
    public void pointCut() {

    }

    /**
     * 处理限流逻辑
     *
     * @param joinPoint
     * @throws NoSuchMethodException
     */
    @Before(value = "pointCut()")
    public void before(JoinPoint joinPoint) throws NoSuchMethodException {
        // 获取当前拦截类
        final Class<?> currentClass = joinPoint.getSignature().getDeclaringType();
        // 获取当前拦截方法
        MethodSignature currentMethod = (MethodSignature) joinPoint.getSignature();
        if (currentMethod.getMethod().isAnnotationPresent(RateLimitIgnore.class)) {
            log.info("忽略执行[{}]-[{}]的限流处理>>>>>>>>>>>>>>>>>>>>>>", currentClass.getName(), currentMethod.getName());
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
                log.debug("[{}-{}]未开启限流限流>>>>>>>>>>>>>>>>>>>>>>", currentClass.getName(), currentMethod.getName());
                return;
            }
            rules = Collections.singletonList(annotation);
        }
        if (CollectionUtil.isEmpty(rules)) {
            log.debug("[{}-{}]限流规则为空>>>>>>>>>>>>>>>>>>>>>>", currentClass.getName(), currentMethod.getName());
            return;
        }

        // 允许多个限流规则存在，如接口全局限流以及也同时需要控制用户级别的防刷
        for (RateLimit annotation : rules) {
            // 处理扩展接口， 可使用外部特性时时刷新属性, 如使用Spring-Cloud的配置时时刷新特性
            // 这个特性基本上只会用在全局限流规则， 其它个性化的限流规则，其实应该用不到这个，不过这里代码统一，反正只要使用的时候，
            // 自己在方法级别设置自己的限流规则就行了
            if (rateLimitProperties.isCloudRefresh()) {
                if (Objects.isNull(rateLimitPropertiesCollect)) {
                    throw new NoSuchBeanDefinitionException(String.format("当使用了cloudRefresh=true时， 请务必同时实现接口[%s]",
                            RateLimitPropertiesCollect.class.getName()));
                }
                // 使用外部接口类填充全局属性
                rateLimitPropertiesCollect.copyToProperties(rateLimitProperties);
            }
            // 属性检查
            rateLimitProperties.check();
            // 获取限流最大令牌桶数量
            Integer max = annotation.max() == rateLimitProperties.getMax() ? rateLimitProperties.getMax() : annotation.max();
            if (Objects.equals(RateLimitProperties.NOT_CONTROL, max)) {
                continue;
            }
            // 获取key生成器
            final String keyGenerator = StringUtils.isBlank(annotation.keyGenerator()) ?
                    rateLimitProperties.getKeyGenerator() : annotation.keyGenerator();
            if (StringUtils.isBlank(keyGenerator)) {
                return;
            }

            // 身份标识 这里如果用户不存在，但是是c端应用的话，可能会有设备号或者之类的标识客户端的唯一身份的，如果有，最好使用这个
            String identityNo = StringUtils.defaultIfBlank(UserContextUtil.getUserId(), UserContextUtil.getCredit());
            // 获取令牌恢复速率
            Integer rate = annotation.rate() == rateLimitProperties.getRate() ? rateLimitProperties.getRate() : annotation.rate();
            if (Objects.equals(RateLimitProperties.NOT_CONTROL, rate)) {
                return;
            }

            // 强制性校验，避免隐藏错误
            if (!KEY_GENERATOR_MAP.containsKey(keyGenerator)) {
                throw new NoSuchBeanDefinitionException(String.format("限流组件[%s]不存在", keyGenerator));
            }

            // 生成限流的key
            String key = KEY_GENERATOR_MAP.get(keyGenerator).generateKey(joinPoint, annotation, rateLimitProperties);
            if (!redisTemplateHelper.tokenBucketRateLimitAcquire(key, max, rate)) {
                log.error("接口【{}-{}-{}】超过限流组件{}预定流量，过滤请求， 完整key规则为: {}, 对应参数{}, 记录日志>>>>>>>", identityNo, currentClass.getName(),
                        currentMethod.getName(), keyGenerator, key, AopUtil.serializeParam(joinPoint)
                );
                throw new BusinessException(LimitExceptionCode.RATE_LIMIT);
            }
        }
    }

    /**
     * 处理条件表达式, 满足条件的才会限流
     *
     * @param joinPoint
     * @param annotation
     * @param currentMethod
     * @return
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
            if (value instanceof Boolean) {
                return (boolean) value;
            }
        }
        return true;
    }
}
