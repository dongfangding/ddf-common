package com.ddf.boot.common.alarm.model;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2025/01/11 20:44
 */
@Data
public class LarkContentRequest implements Serializable {

    /**
     * 富文本内容， 这个字段包含的内容是content字段
     * {
     * "msg_type": "post",
     * "content": {
     * "post": {
     * "zh_cn": {
     * "title": "项目更新通知",
     * "content": [
     * [{
     * "tag": "text",
     * "text": "项目有更新: "
     * },
     * {
     * "tag": "a",
     * "text": "请查看",
     * "href": "http://www.example.com/"
     * },
     * {
     * "tag": "at",
     * "user_id": "ou_18eac8********17ad4f02e8bbbb"
     * }
     * ]
     * ]
     * }
     * }
     * }
     * }
     */
    private List<List<LarkTag>> content;
}
