package com.ddf.boot.common.api.validator.constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

/**
 * <p>针对业务字段值进行有效值校验
 * </p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/02/26 10:53
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {LogicValueValidator.ActiveValidatorImpl.class})
@Documented
public @interface LogicValueValidator {

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
    int[] values() default {0, 1};

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
    class ActiveValidatorImpl implements ConstraintValidator<LogicValueValidator, Integer> {

        /**
         * 有效值
         **/
        private Set<Integer> values = Collections.emptySet();

        /**
         * 初始化参数
         *
         * @param constraintAnnotation
         */
        @Override
        public void initialize(LogicValueValidator constraintAnnotation) {
            if (constraintAnnotation.values().length > 0) {
                values = new HashSet<>(constraintAnnotation.values().length);
                for (int value : constraintAnnotation.values()) {
                    values.add(value);
                }
            }
        }

        /**
         *
         * @param value   object to validate
         * @param context context in which the constraint is evaluated
         * @return {@code false} if {@code value} does not pass the constraint
         */
        @Override
        public boolean isValid(Integer value, ConstraintValidatorContext context) {
            return Objects.isNull(value) || values.contains(value);
        }
    }
}
