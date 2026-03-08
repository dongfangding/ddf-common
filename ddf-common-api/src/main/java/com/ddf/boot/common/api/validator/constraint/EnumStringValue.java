package com.ddf.boot.common.api.validator.constraint;


import com.ddf.boot.common.api.enums.IEnum;
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
 * 枚举值校验注解，支持针对不同枚举类型的字段验证。
 *
 * @author snowball
 * @since 2025/11/17 11:39
 **/
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumStringValue.EnumStringValueValidator.class)
@Documented
public @interface EnumStringValue {

    /**
     * 错误消息
     */
    String message() default "字段值不在有效值范围内";

    /**
     * 是否必传参数， 如果是的话， 不能为空
     *
     * @return
     */
    boolean required() default true;

    /**
     * 校验值的枚举类
     */
    Class<? extends IEnum<?>> enumClass();

    /**
     * 即使用不到也要保留，否则会报错
     */
    Class<?>[] groups() default {};

    /**
     * 即使用不到也要保留，否则会报错
     */
    Class<? extends Payload>[] payload() default {};


    class EnumStringValueValidator implements ConstraintValidator<EnumStringValue, String> {

        private Class<? extends IEnum<?>> enumClass;
        private boolean required;

        @Override
        public void initialize(EnumStringValue constraintAnnotation) {
            // 获取注解中的枚举类型
            enumClass = constraintAnnotation.enumClass();
            required = constraintAnnotation.required();
        }
        /**
         * @param value 参数
         * @param context 参数
         */
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (StringUtils.isBlank(value) && required) {
                return false;
            }

            // 获取枚举常量数组
            IEnum<?>[] enumConstants = enumClass.getEnumConstants();

            // 遍历枚举常量，检查值是否存在
            for (IEnum<?> enumConstant : enumConstants) {
                if (enumConstant
                    .getValue()
                    .equals(value)) {
                    return true;
                }
            }
            return false;
        }
    }
}
