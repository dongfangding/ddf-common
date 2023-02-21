package com.ddf.boot.common.redis.response;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>基于hash的自增且上限判定返回类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/09/27 18:09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HashIncrementCheckResponse implements Serializable {

    private static final long serialVersionUID = 3718353983577760216L;

    /**
     * 自增之后返回值，注意只是自增之后的返回值，不一定是实际保存的值，因为如果达到上限会回滚本次增加的值，这里只是返回给上层
     */
    private Long result;

    /**
     * 自增之后的实际值， 如果自增之后达到上限值，会回滚本次自增值，这个就是本次回滚之后的值
     */
    private Long actualResult;

    /**
     * 本次自增是否超过限定值
     *
     * @return
     */
    public boolean isLimit() {
        return result > actualResult;
    }
}
