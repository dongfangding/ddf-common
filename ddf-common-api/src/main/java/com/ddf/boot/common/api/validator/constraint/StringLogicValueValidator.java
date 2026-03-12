package com.ddf.boot.common.api.validator.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>针对业务字段值进行有效值校验
 * </p >
 *
 * @author snowball
 * @version 1.0
 * @since 2021/02/26 10:53
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {StringLogicValueValidator.ActiveValidatorImpl.class})
@Documented
public @interface StringLogicValueValidator {

    /**
     * 错误消息
     *
     * @return
     */
    String message() default "逻辑字段的有效值只能是0和1";

    /**
     * 有效值, 默认0 和 1
     *
     * @return
     */
    String[] values() default {"0", "1"};

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
     * 验证器实现
     *
     */
    class ActiveValidatorImpl implements ConstraintValidator<StringLogicValueValidator, String> {

        /**
         * 有效值
         **/
        private Set<String> values = Collections.emptySet();

        /**
         * 初始化参数
         *
         * @param constraintAnnotation 约束注解对象
         */
        @Override
        public void initialize(StringLogicValueValidator constraintAnnotation) {
            if (constraintAnnotation.values().length > 0) {
                values = new HashSet<>(constraintAnnotation.values().length);
                values.addAll(Arrays.asList(constraintAnnotation.values()));
            }
        }

        /**
         *
         * @param value   参数值
         * @param context 上下文参数
         * @return {@code false} if {@code value} does not pass the constraint
         */
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return StringUtils.isBlank(value) || values.contains(value);
        }
    }
}
