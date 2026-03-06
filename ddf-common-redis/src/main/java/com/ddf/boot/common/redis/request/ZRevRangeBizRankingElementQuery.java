package com.ddf.boot.common.redis.request;

import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2026/01/26 15:55
 */
@Data
public class ZRevRangeBizRankingElementQuery {

    /**
     * 榜单key
     */
    private String rankingKey;
    /**
     * 存榜单每个element的详情业务信息key
     */
    private String detailKey;
    /**
     * 要添加的element
     */
    private String element;
    /**
     * 分数工厂，用于计算element的分数
     */
    private Integer scoreFactory;
}
