package com.ddf.boot.common.api.validator.constraint;

import cn.hutool.core.util.IdcardUtil;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

/**
 * <p>身份证号码验证器</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/02/26 11:56
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
     * 身份证号码校验器
     */
    class IdCardValidator implements ConstraintValidator<IdCard, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return IdcardUtil.isValidCard(value);
        }
    }
}
