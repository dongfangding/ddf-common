package com.ddf.common.websocket.constant;

import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

/**
 * 常量
 *
 * @author dongfang.ding
 * @date 2019/10/26 15:04
 */
public class WebsocketConst {

    /** ws的默认处理器入门映射地址 */
    public static final String DEFAULT_ENDPOINT = "/pay-ws";

    /** 认证的token参数名称，放在header中 */
    public static final String TOKEN_PARAMETER = "token";

    /** 认证的ime参数名称，放在header中 */
    public static final String IME_PARAMETER = "ime";

    /** 认证用户在作用域存放时的key */
    public static final String PRINCIPAL_KEY = "principal";

    /** 握手时从请求头中获取到的真实IP */
    public static final String CLIENT_REAL_IP = "clientAddress";

    /** 握手时从请求中获取到的本机IP */
    public static final String SERVER_IP = "serverIp";

    /** cmd命令转发的发布主题 */
    public static final String REDIRECT_CMD_TOPIC = "redirect_cmd";

    /** cmd命令转发后机器处理完成后返回的数据 */
    public static final String RETURN_MESSAGE_TOPIC = "return_message";

    /**
     * 认证来源参数
     */
    public static final String LOGIN_TYPE_PARAMETER = "loginType";

    /** 银行卡冻结key */
    public static final String FROZEN_BANK_CARD = "FROZEN:BANK:CARD:";

    /**
     * @see ConcurrentWebSocketSessionDecorator
     */
    public static final int SEND_TIME_LIMIT = 3000;

    /**
     * @see ConcurrentWebSocketSessionDecorator
     */
    public static final int BUFFER_SIZE_LIMIT = 102400;
}
