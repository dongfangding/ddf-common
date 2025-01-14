package com.ddf.boot.common.redis.response;

import java.io.Serializable;
import lombok.Data;

/**
 * <p>访问限流返回类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2025/01/09 11:35
 */
@Data
public class AccessLimitResponse implements Serializable {

    /**
     * 是否被限流
     */
    private boolean limited;

    /**
     * 当前次数，不会大于最大限制
     */
    private Long currentCount;

    /**
     * 最大限制
     */
    private Long maxCount;
}
