package com.ddf.boot.common.api.template;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * <p>炸金花牌型</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2025/04/19 15:29
 */
@Getter
public enum JinHuaCardTypeEnum {

    TYPE_HIGH_CARD("TYPE_HIGH_CARD", "高牌"),
    TYPE_PAIR("TYPE_PAIR", "对子"),
    TYPE_STRAIGHT("TYPE_STRAIGHT", "顺子"),
    TYPE_FLUSH("TYPE_FLUSH", "同花"),
    TYPE_STRAIGHT_FLUSH("TYPE_STRAIGHT_FLUSH", "同花顺"),
    TYPE_THREE_OF_A_KIND("TYPE_THREE_OF_A_KIND", "豹子"),


    ;

    private final String type;
    private final String desc;

    private static final Map<String, JinHuaCardTypeEnum> MAPPINGS;

    static {
        MAPPINGS = Arrays
                .stream(values())
                .collect(Collectors.toMap(JinHuaCardTypeEnum::getType, obj -> obj));

    }
    JinHuaCardTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }
    /**
     * @param value 参数值
     */
    public static JinHuaCardTypeEnum resolve(String value) {
        return MAPPINGS.get(value);
    }

}
