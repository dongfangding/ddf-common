package com.ddf.common.boot.mqtt.model.support.body;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>抽象消息body, 主要是增加消息标题功能</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/21 18:35
 */
@Data
public class AbstractMessageBody implements MessageBody {

    /**
     * 文本内容
     */
    private String msg;

    /**
     * 消息标题
     */
    private String msgTitle;

    /**
     * 消息内容
     */
    private String contentType;

    @Override
    public String getMsg() {
        return msg;
    }

    public String getMsgTitle() {
        return StringUtils.defaultString(msgTitle, StringUtils.isNotBlank(msg) ? msg.substring(20) : "");
    }
}
