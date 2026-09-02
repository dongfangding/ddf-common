package com.ddf.boot.common.core.util;

import com.ddf.boot.common.api.model.dto.JinHuaCard;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JinHuaUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class JinHuaUtilTest {

    @Test
    @DisplayName("超出最大玩家数时应返回空列表")
    void shouldReturnEmptyListWhenPlayerCountExceedsLimit() {
        assertTrue(JinHuaUtil.randomCard(18).isEmpty());
    }

    @Test
    @DisplayName("正常发牌时应返回指定数量且按分数降序")
    void shouldReturnSortedHandsWithThreeCardsEach() {
        List<JinHuaCard> result = JinHuaUtil.randomCard(5);

        assertEquals(5, result.size());
        assertTrue(result.stream().allMatch(card -> card.getCards() != null && card.getCards().size() == 3));
        assertTrue(result.stream().allMatch(card -> card.getCardType() != null));
        assertTrue(result.stream().allMatch(card -> card.getCardScore() != null));

        for (int i = 1; i < result.size(); i++) {
            assertTrue(result.get(i - 1).getCardScore() > result.get(i).getCardScore(),
                    "第" + i + "张牌分数应小于第" + (i - 1) + "张牌（降序排列）");
        }
    }
}
