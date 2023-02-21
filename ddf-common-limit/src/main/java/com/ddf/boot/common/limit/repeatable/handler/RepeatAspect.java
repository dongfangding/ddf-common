package com.ddf.boot.common.limit.repeatable.handler;

import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.authentication.util.UserContextUtil;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.ddf.boot.common.core.util.AopUtil;
import com.ddf.boot.common.limit.exception.LimitExceptionCode;
import com.ddf.boot.common.limit.repeatable.annotation.Repeatable;
import com.ddf.boot.common.limit.repeatable.annotation.RepeatableIgnore;
import com.ddf.boot.common.limit.repeatable.config.RepeatableProperties;
import com.ddf.boot.common.limit.repeatable.validator.RepeatableValidator;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>表单防重校验</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/05 11:40
 */
@Aspect
@Slf4j
public class RepeatAspect {

    public static final String BEAN_NAME = "repeatAspect";

    @Autowired
    private RepeatableProperties repeatableProperties;

    /**
     * 表单防重校验接口实现
     */
    private final Map<String, RepeatableValidator> handlerMapping = SpringContextHolder.getBeansOfType(
            RepeatableValidator.class);

    @Pointcut(value = "@annotation(com.ddf.boot.common.limit.repeatable.annotation.Repeatable) || @within(com.ddf.boot.common.limit.repeatable.annotation.Repeatable)")
    public void pointCut() {

    }

    @Before(value = "pointCut()")
    public void before(JoinPoint joinPoint) throws NoSuchMethodException {
        // 获取当前拦截类
        final Class<?> currentClass = joinPoint.getSignature().getDeclaringType();
        // 获取当前拦截方法
        MethodSignature currentMethod = (MethodSignature) joinPoint.getSignature();
        // 可能会有些接口不需要登录，无法拿到用户id, 则拿设备编号
        // 身份标识
        String identityNo = StringUtils.defaultIfBlank(UserContextUtil.getUserId(), UserContextUtil.getCredit());

        // 判断当前方法是否需要忽略， 因为@Repeatable是可以加在类上的， 这里想支持个别忽略
        final boolean ignore = currentMethod.getMethod().isAnnotationPresent(RepeatableIgnore.class);
        if (ignore) {
            log.info("忽略执行[{}-{}]-[{}]的防重复提交检查>>>>>>>>>>>>>>>>>>>>>>", identityNo, currentClass.getName(), currentMethod.getName());
            return;
        }
        log.info("开始执行[{}, {}]-[{}]的防重复提交检查>>>>>>>>>>>>>>>>>>>>>>", identityNo, currentClass.getName(), currentMethod.getName());

        // 处理是否需要检查
        final Repeatable annotation = AopUtil.getAnnotation(joinPoint, Repeatable.class);

        // 获取校验器
        String validator = StringUtils.defaultIfBlank(annotation.validator(), repeatableProperties.getGlobalValidator());
        if (!handlerMapping.containsKey(validator)) {
            throw new NoSuchBeanDefinitionException(validator, "请检查校验器实现bean是否存在");
        }

        // 执行校验逻辑
        final boolean check = handlerMapping.get(validator).check(joinPoint, annotation, identityNo, repeatableProperties);
        if (!check && annotation.throwError()) {
            log.info("接口【{}-{}-{}】对应参数【{}】请求过于频繁， 记录日志>>>>>>>", identityNo, currentClass.getName(),
                    currentMethod.getName(), AopUtil.serializeParam(joinPoint));
            throw new BusinessException(LimitExceptionCode.REPEAT_SUBMIT);
        }
    }

}
