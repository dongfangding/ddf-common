package com.ddf.boot.common.websocket.constant;

import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

/**
 * 常量
 *
 * @author dongfang.ding
 * @date 2019/10/26 15:04
 */
public class WebsocketConst {

    /** ws的默认处理器入门映射地址 */
    public static final String DEFAULT_ENDPOINT = "/ddf-ws";

    /** 认证的token参数名称，放在header中 */
    public static final String TOKEN_PARAMETER = "token";

    /** 认证用户在作用域存放时的key */
    public static final String PRINCIPAL_KEY = "principal";

    /** 认证token多个参数的分隔符 */
    public static final String AUTH_SPLIT = ";";

    /** 认证参数中的毫秒值有效毫秒值 */
    public static final long VALID_AUTH_TIMESTAMP = 60 * 1000 * 5;

    /** 握手时从请求头中获取到的真实IP */
    public static final String CLIENT_REAL_IP = "clientAddress";

    /** 握手时从请求中获取到的本机IP */
    public static final String SERVER_IP = "serverIp";

    /** cmd命令转发的发布主题 */
    public static final String REDIRECT_CMD_TOPIC = "redirect_cmd";

    /** cmd命令转发后机器处理完成后返回的数据 */
    public static final String RETURN_MESSAGE_TOPIC = "return_message";

    /** 提醒消息cmd命令转发的发布主题 */
    public static final String REDIRECT_CMD_NOTICE_TOPIC = "redirect_cmd_notice";

    /** 提示消息转发的发布主题 */
    public static final String REDIRECT_MSG_NOTICE_TOPIC = "redirect_msg_notice";

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

    /** byte类型的true */
    public static final Byte BYTE_TRUE = 1;

    /** byte类型的false */
    public static final Byte BYTE_FALSE = 0;

    /** Integer类型的true */
    public static final Integer INTEGER_TRUE = 1;

    /** Integer类型的false */
    public static final Integer INTEGER_FALSE = 0;

    /**
     * 设备每日余额检查最大次数
     */
    public static final String AMOUNT_CHECK_DAILY_MAX_TIMES = "amount_check_daily_max_times";

    /**
     * 余额检查最小发送时间间隔
     */
    public static final String AMOUNT_CHECK_SEND_MINUTES_INTERVAL = "amount_check_send_minutes_interval";

    /**
     * 余额检查非参数类控制时，随机虽小间隔时间
     */
    public static final int AMOUNT_CHECK_RANDOM_MIN = 24 * 60;
    /**
     * 余额检查非参数类控制时，随机最大间隔时间
     */
    public static final int AMOUNT_CHECK_RANDOM_MAX = 30 * 60;


    /**
     * 数据同步的最小间隔
     */
    public static final int DATA_SYNC_INTERVAL = 12 * 60;

    /**
     * 检查客户端是否是最新版本的最小时间间隔
     */
    public static final int APP_VERSION_SYNC = 20;

    /**
     * 因二维码指令未单独定义出来一个对象用来解析，暂时定义一个常量代表存连接的字段
     */
    public static final String QR_CODE_CONTENT = "content";

    public static final String DUBBO_VERSION = "1.0.0";
}
