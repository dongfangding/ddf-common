package com.ddf.boot.common.api.validator.constraint;

import cn.hutool.core.lang.Validator;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>是否为有效手机号校验器</p >
 *
 * @author snowball
 * @version 1.0
 * @date 2021/02/26 11:56
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {Mobile.MobileValidator.class})
@Documented
public @interface Mobile {

    /**
     * 错误消息
     *
     * @return
     */
    String message() default "手机号不合法";

    /**
     * 有效值, 默认0 和 1
     *
     * @return
     */
    String values() default "";

    /**
     * 即使用不到也要保留，否则会报错
     * @return
     */
    Class<?>[] groups() default { };

    /**
     * 即使用不到也要保留，否则会报错
     * @return
     */
    Class<? extends Payload>[] payload() default { };

    /**
     * 手机号校验器
     */
    class MobileValidator implements ConstraintValidator<Mobile, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            // 为空时不进行逻辑校验
            if (StringUtils.isBlank(value)) {
                return Boolean.TRUE;
            }
            return Validator.isMobile(value);
        }
    }
}
