package com.ddf.boot.common.redis.response;

import java.io.Serializable;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2025/11/24 11:46
 */
@Data
public class HashDecreaseUntilFirstLessThanZeroResponse implements Serializable {

    /**
     * 是否被限流
     */
    private boolean limited;

    /**
     * 当前值
     */
    private Long currentValue;
}
