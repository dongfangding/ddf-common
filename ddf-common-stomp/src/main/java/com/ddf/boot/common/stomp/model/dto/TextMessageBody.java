package com.ddf.boot.common.stomp.model.dto;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * <p>文本消息内容</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/03/21 18:35
 */
@Data
public class TextMessageBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文本内容
     */
    private String msg;

    /**
     * 消息标题
     */
    private String msgTitle;
}
