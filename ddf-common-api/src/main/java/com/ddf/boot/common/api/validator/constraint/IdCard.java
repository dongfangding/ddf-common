package com.ddf.boot.common.api.validator.constraint;

import cn.hutool.core.util.IdcardUtil;
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
 * <p>身份证号码验证器</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2021/02/26 11:56
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {IdCard.IdCardValidator.class})
@Documented
public @interface IdCard {

    /**
     * 错误消息
     *
     * @return
     */
    String message() default "身份证号不合法";

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
     * 身份证号码校验器
     */
    class IdCardValidator implements ConstraintValidator<IdCard, String> {

        /**
         * @param value 参数
         * @param context 参数
         */
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return IdcardUtil.isValidCard(value);
        }
    }
}