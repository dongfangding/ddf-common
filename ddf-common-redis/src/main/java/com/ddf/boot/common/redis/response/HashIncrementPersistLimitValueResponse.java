package com.ddf.boot.common.redis.response;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>基于hash的自增，超出上下限则将值设置为上下限，而不是回滚的响应结果</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2022/09/27 18:09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HashIncrementPersistLimitValueResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 3718353983577760216L;

    // limited = 1, actualStep = minValue - (result - step), currentValue = minValue
    /**
     * 是否触发了上下限
     */
    private boolean limited;

    /**
     * 存入后的最新数值
     */
    private Long currentValue;

    /**
     * 实际递增了多少数值，如果递增超出了上下限，这里会返回实际递增的值，如旧的数值是19， 上限是20， 本地递增2， 那么这里返回的就是1
     */
    private Double actualStep;
}
