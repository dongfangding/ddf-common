package com.ddf.common.boot.mqtt.model.support.body;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>抽象消息body, 主要是增加消息标题功能</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/21 18:35
 */
public class AbstractMessageBody implements MessageBody {

    /**
     * 文本内容
     */
    private String msg;

    /**
     * 消息标题
     */
    private String msgTitle;

    @Override
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsgTitle() {
        return StringUtils.defaultString(msgTitle, msg);
    }

    public void setMsgTitle(String msgTitle) {
        this.msgTitle = msgTitle;
    }
}
