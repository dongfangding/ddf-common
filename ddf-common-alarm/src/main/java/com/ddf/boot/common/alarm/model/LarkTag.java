package com.ddf.boot.common.alarm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2025/01/11 19:37
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class LarkTag {

    /**
     * 标签类型
     */
    private String tag;

    /**
     * 内容
     */
    private String text;

    /**
     * 链接
     */
    private String href;

    /**
     * 艾特
     */
    @JsonProperty("user_id")
    private String userId;

    public static LarkTag buildText(String content) {
        return LarkTag.of("text", content, "", "");
    }

    /**
     * @param href 参数
     */
    public static LarkTag buildHref(String href) {
        return LarkTag.of("a", "", href, "");
    }

    /**
     * @param userId 参数
     */
    public static LarkTag buildAt(String userId) {
        return LarkTag.of("at", "", "", userId);
    }

    public static LarkTag buildAtAll() {
        return LarkTag.of("at", "", "", "all");
    }
}
