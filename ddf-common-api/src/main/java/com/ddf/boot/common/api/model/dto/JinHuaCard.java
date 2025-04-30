package com.ddf.boot.common.api.model.dto;

import java.util.List;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2025/04/19 17:21
 */
@Data
public class JinHuaCard {

    /**
     * 牌型
     */
    private String cardType;

    /**
     * 具体牌面
     */
    private List<PokerCard> cards;

    /**
     * 本副牌的得分
     */
    private Integer cardScore;


}
