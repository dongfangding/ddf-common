package com.ddf.boot.common.api.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>扑克牌</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2025/04/19 15:26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class PokerCard {

    private Integer originCardId;

    /**
     * 牌值 对应2~14
     */
    private Integer cardId;

    /**
     * 牌值，对应2~A
     */
    private String cardValue;

    /**
     * 牌的花色
     */
    private String cardColor;

}
