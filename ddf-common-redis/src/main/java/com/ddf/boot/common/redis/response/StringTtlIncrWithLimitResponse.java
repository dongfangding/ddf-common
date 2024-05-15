package com.ddf.boot.common.redis.response;

import java.io.Serializable;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2023/08/10 11:22
 */
@Data
public class StringTtlIncrWithLimitResponse implements Serializable {

    private static final long serialVersionUID = 1516322558409231083L;

    /**
     * 当前key剩余ttl
     */
    private Integer ttl;

    /**
     * 当前key的ttl是否达到过最大值， 如果达到过且ttl一直未到0，这个值就是true, 到0之后，再重新设置ttl之后就是false
     */
    private boolean full;

}
