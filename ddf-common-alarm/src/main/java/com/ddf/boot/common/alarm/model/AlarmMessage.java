package com.ddf.boot.common.alarm.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 结构化告警消息，各渠道按自身能力渲染（钉钉渲染为 markdown，Lark 渲染为富文本卡片）。
 *
 * @author snowball
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmMessage {

    /**
     * 告警标题
     */
    private String title;

    /**
     * 有序内容行，每行自包含一段信息
     */
    private List<String> lines;
}
