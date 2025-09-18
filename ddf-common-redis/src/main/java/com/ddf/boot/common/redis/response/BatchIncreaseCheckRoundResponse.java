package com.ddf.boot.common.redis.response;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * <p>访问限流返回类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2025/01/09 11:35
 */
@Data
public class BatchIncreaseCheckRoundResponse implements Serializable {

    /**
     * 是否被限流
     */
    private boolean limited;

    /**
     * 当前次数，不会大于最大限制
     */
    private List<Long> currentCount;
}
