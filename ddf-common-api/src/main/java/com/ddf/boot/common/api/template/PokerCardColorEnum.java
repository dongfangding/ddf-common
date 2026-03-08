package com.ddf.boot.common.api.template;

import java.util.Arrays;
import lombok.Getter;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2025/04/19 15:29
 */
@Getter
public enum PokerCardColorEnum {

    SPADES("SPADES", "黑桃"),
    HEARTS("HEARTS", "红桃"),
    CLUBS("CLUBS", "梅花"),
    DIAMONDS("DIAMONDS", "方块"),


    ;

    /**
     * @param color 参数
     * @param desc 参数
     */
    private final String color;
    private final String desc;

    PokerCardColorEnum(String color, String desc) {
        this.color = color;
        this.desc = desc;
    }

    public static String[] getColorArray() {
        return Arrays
                .stream(values()).map(PokerCardColorEnum::getColor).toArray(String[]::new);
    }
}