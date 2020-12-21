package com.ddf.boot.common.websocket.enumu;

import java.io.Serializable;

/**
 * 内置指令码
 *
 * @author dongfang.ding
 * @date 2020/3/11 0011 15:59
 */
public enum InternalCmdEnum implements Serializable {

    /**
     * 心跳包指令
     */
    PING,

    /**
     * pong
     */
    PONG,

}
