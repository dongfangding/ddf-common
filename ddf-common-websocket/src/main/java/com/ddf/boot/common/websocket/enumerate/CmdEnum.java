package com.ddf.boot.common.websocket.enumerate;

import java.io.Serializable;

/**
 * 下发指令的命令码和子命令码
 *
 * 针对一些业务复杂需要携带数据的指令，每个指令都是一个指令码即具备含义；
 * 但还有一些简单的指令码，如禁用，登出等，不需要携带业务数据，为了避免命令码过多，
 * 提供一个命令码SIMPLE,通过子命令码来确定具体含义
 *
 *
 * @author dongfang.ding
 * @date 2020/3/11 0011 15:59
 *
 */
public enum CmdEnum implements Serializable {
    //----------------------------------------------服务端指令
    /**
     * pong
     */
    PONG,

    /**
     * 设备重启
     */
    RESTART,

    /**
     * APP升级
     */
    UPGRADE,
    /**
     * IP/GPS切换
     */
    SWITCH_IP_GPS,
    /**
     * 二维码生成
     */
    QRCODE_CREATE,

    /**
     * 查看云闪付账单
     *
     *
     * replace by {@link CmdEnum#FETCH_BILL}
     */
    @Deprecated
    UPAY_BILL,

    /**
     * 查看账单
     */
    FETCH_BILL,


    /**
     * 云闪付到账消息不一定能够收到，因此服务端会主动去请求一次
     *
     * replace by {@link CmdEnum#BILL_MATCH_ORDER}
     */
    @Deprecated
    UPAY_BILL_ORDER,

    /**
     * 账单匹配订单
     */
    BILL_MATCH_ORDER,

    /**
     * 登录云闪付
     */
    LOGIN,


    /**
     * 简单指令合集，具体指令需要参考子命令，没有数据的指令，如禁用、登出等
     */
    SIMPLE,

    /**
     * 验证码
     */
    VERIFY_CODE,

    /**
     * 二维码预生成
     * 支持商户码以及普通码方便调试
     */
    QRCODE_CREATE_PRE,


    /**
     * 金额校准
     */
    AMOUNT_CHECK,

    /**
     * 提现转账报文
     */
    PAY,


    /**
     * 与设备进行资料信息同步，如设备所属人个人信息， 银行卡信息
     */
    DATA_SYNC,


    /**
     * 服务端委托客户端发送一些数据的通道
     */
    SEND_MSG,


    //----------------------------------------------客户端指令

    /**
     * ping
     */
    PING,

    /**
     * APP 最新版本
     */
    APP_LAST_VERSION,

    /**
     * 云闪付到账消息
     *
     * replace by {@link CmdEnum#TOPIC_MESSAGE}
     */
    @Deprecated
    UPAY_MESSAGE,

    /**
     * 到账通知消息
     */
    TOPIC_MESSAGE,

    /**
     * 银行收款短信
     *
     * replace by {@link CmdEnum#SMS_UPLOAD}
     *
     */
    @Deprecated
    BANK_SMS,

    /**
     * 设备短信上传
     */
    SMS_UPLOAD,


    /**
     * 运行状态报备，客户端对服务端所需的应用状态进行监听，当状态变化时要向服务端报备最新状态
     */
    RUNNING_STATE,

    /**
     * 客户端异常上报
     */
    CLIENT_ERROR_UPLOAD,

    /**
     * 银行卡绑定
     */
    BINDING_CARD,

    /**
     * 支付方式注册
     */
    REGISTRY,

    /**
     * 设置支付密码
     */
    SET_PAY_PASSWORD,

    /**
     * 设置商户码
     */
    SET_MERCHANT_QRCODE

    ;




    /**
     * 子命令合集，针对SIMPLE指令
     */
    public enum ChildEnum {
        /**
         * 禁用设备
         */
        DISABLE,
        /**
         * 登出
         */
        LOGOUT,
        /**
         * 启用设备
         */
        ENABLE
    }
}
