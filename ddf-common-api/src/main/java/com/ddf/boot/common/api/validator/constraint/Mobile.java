package com.ddf.boot.common.api.validator.constraint;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>是否为有效手机号校验器</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2021/02/26 11:56
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {Mobile.MobileValidator.class})
@Documented
public @interface Mobile {

    /**
     * 错误消息
     */
    String message() default "手机号不合法";

    /**
     * 有效值, 默认0 和 1
     */
    String values() default "";

    /**
     * 即使用不到也要保留，否则会报错
     */
    Class<?>[] groups() default {};

    /**
     * 即使用不到也要保留，否则会报错
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * 手机号校验器
     */
    class MobileValidator implements ConstraintValidator<Mobile, String> {

        /**
         * @param value 参数值
         * @param context 参数
         */
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            // 为空时不进行逻辑校验
            if (StrUtil.isBlank(value)) {
                return Boolean.TRUE;
            }
            return Validator.isMatchRegex(Validator.MOBILE, value);
        }
    }
}
