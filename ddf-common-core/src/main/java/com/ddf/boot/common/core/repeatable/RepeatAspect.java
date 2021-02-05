package com.ddf.boot.common.core.repeatable;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.ddf.boot.common.core.exception200.BusinessException;
import com.ddf.boot.common.core.exception200.GlobalCallbackCode;
import com.ddf.boot.common.core.util.AopUtil;
import com.ddf.boot.common.core.util.JsonUtil;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/02/05 11:40
 */
@Aspect
@Slf4j
public class RepeatAspect {

    @Autowired
    private RepeatableProperties repeatableProperties;

    /**
     * 表单防重校验接口实现
     */
    private final Map<String, RepeatableValidator> handlerMapping =
            SpringUtil.getApplicationContext().getBeansOfType(RepeatableValidator.class);

    @Pointcut(value = "@annotation(com.ddf.boot.common.core.repeatable.Repeatable) || @within(com.ddf.boot.common.core.repeatable.Repeatable)")
    public void pointCut() {

    }

    @Before(value = "pointCut()")
    public void before(JoinPoint joinPoint) throws NoSuchMethodException {
        // 获取当前拦截类
        final Class<?> currentClass = joinPoint.getSignature().getDeclaringType();
        // 获取当前拦截方法
        MethodSignature currentMethod = (MethodSignature) joinPoint.getSignature();
        // 可能会有些接口不需要登录，无法拿到用户id, 简单处理使用随机值
        String currentUid = "";

        // 处理是否需要检查
        final Repeatable annotation = AopUtil.getAnnotation(joinPoint, Repeatable.class);
        // 判断当前方法是否需要忽略， 因为@Repeatable是可以加在类上的， 这里想支持个别忽略
        final boolean ignore = currentMethod.getMethod().isAnnotationPresent(RepeatableIgnore.class);
        if (ignore) {
            log.info("忽略执行[{}-{}]-[{}]的防重复提交检查>>>>>>>>>>>>>>>>>>>>>>", currentUid, currentClass.getName(), currentMethod.getName());
            return;
        }
        log.info("开始执行[{}, {}]-[{}]的防重复提交检查>>>>>>>>>>>>>>>>>>>>>>", currentUid, currentClass.getName(), currentMethod.getName());

        // 获取校验器
        String volidator = repeatableProperties.getGlobalValidator();
        if (StrUtil.isNotBlank(annotation.validator())) {
            volidator = annotation.validator();
        }
        if (!handlerMapping.containsKey(volidator)) {
            throw new NoSuchBeanDefinitionException(volidator, "请检查校验器实现bean是否存在");
        }

        // 执行校验逻辑
        final boolean check = handlerMapping.get(volidator).check(joinPoint, annotation, currentUid);
        if (!check && annotation.throwError()) {
            log.info("接口【{}-{}-{}】对应参数【{}】请求过于频繁， 记录日志>>>>>>>", currentUid, currentClass.getName(),
                    currentMethod.getName(), JsonUtil.asString(AopUtil.getParamMap(joinPoint)));
            throw new BusinessException(GlobalCallbackCode.REPEAT_SUBMIT);
        }
    }

}
