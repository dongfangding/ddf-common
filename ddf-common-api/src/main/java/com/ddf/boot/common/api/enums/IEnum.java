
package com.ddf.boot.common.api.enums;

import java.util.Objects;

/**
 * @author yiming
 *
 */
public interface IEnum<T> extends ValueBean<T> {

    /**
     * 是否匹配
     * @param value 参数值
     * @return
     */
    default boolean matches(T value) {
        return Objects.nonNull(value) && Objects.equals(value, getValue());
    }

    /**
     * 是否匹配
     * @param iEnum 枚举实例
     * @return
     */
    default boolean matches(IEnum<T> iEnum) {
        return Objects.nonNull(iEnum) && Objects.equals(this, iEnum);
    }
}
