package com.ddf.boot.common.redis.response;

import java.io.Serializable;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2025/09/17 16:42
 */
@Data
public class ZsetZaddWithMaxCheckResponse implements Serializable {

    /**
     * 新增的分数
     */
    private Double score;

    /**
     * 是否更新了
     */
    private Integer updated;


    public boolean isChanged() {
        return updated > 0;
    }
}
