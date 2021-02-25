package com.ddf.boot.common.limit.repeatable.validator;

import com.ddf.boot.common.limit.repeatable.annotation.Repeatable;
import com.ddf.boot.common.limit.repeatable.config.RepeatableProperties;
import org.aspectj.lang.JoinPoint;

/**
 * <p>AspectJ实现方式的表单防重复提交验证接口</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/05 11:41
 */
public interface RepeatableValidator {

    /**
     * 执行表单放重校验逻辑
     *
     * @param joinPoint  织入点
     * @param repeatable 注解
     * @param currentUid 用户uid
     * @param repeatableProperties 配置属性
     *
     * @return 是否通过校验
     *
     */
    boolean check(JoinPoint joinPoint, Repeatable repeatable, String currentUid, RepeatableProperties repeatableProperties);

}
