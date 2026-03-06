package com.ddf.boot.common.redis.request;

import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2026/01/23 14:25
 */
@Data
public class ZRevRangeBizRankingQuery {

    /**
     * 榜单key
     */
    private String rankingKey;
    /**
     * 存榜单每个element的详情业务信息key
     */
    private String detailKey;
    /**
     * 存榜单所有element的总分数key
     */
    private String sumKey;

    /**
     * startIndex
     */
    private long startIndex;
    /**
     * endIndex
     */
    private long endIndex;
    /**
     * 分数工厂，用于计算element的分数
     */
    private Integer scoreFactory;

}
