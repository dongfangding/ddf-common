package com.ddf.boot.common.redis.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2026/01/23 16:50
 */
@Data
public class ZRevRangeBizRankingResponse implements Serializable {

    /**
     * 所有上榜元素数
     */
    private Long totalElements;

    /**
     * 所有上榜元素的总score
     */
    private BigDecimal totalScore;

    /**
     * 本次返回结果集
     */
    private List<Element> list;


    @Data
    public static class Element {
        /**
         * 上榜元素
         */
        private String element;

        /**
         * 分数
         */
        private BigDecimal score;

        /**
         * 排名
         */
        private Long rank;

        /**
         * 业务信息
         */
        private String detail;
    }
}
