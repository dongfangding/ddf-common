package com.ddf.boot.common.redis.request;

import java.math.BigDecimal;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2026/01/23 14:25
 */
@Data
public class ZSetAddDoubleWithMaxCheckCommand {
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
     * 要添加的element
     */
    private String element;
    /**
     * 要添加的element的分数
     */
    private BigDecimal score;
    /**
     * 分数工厂，用于计算element的分数
     */
    private Integer scoreFactory;

    /**
     * 分数的小数部分，用来实现二次排序用的，必须是小数。用来解决二次排序时，分数相同的情况，使用小数解决。
     */
    private BigDecimal scoreDecimal;

    /**
     * 过期时间，单位秒
     */
    private Long expireSeconds;
    /**
     * 要添加的element的详情业务信息json
     */
    private String detailJson;
}
