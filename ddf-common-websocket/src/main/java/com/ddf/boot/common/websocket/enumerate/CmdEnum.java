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
 *
 * @author dongfang.ding
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
     */
    UPAY_BILL,


    /**
     * 云闪付到账消息不一定能够收到，因此服务端会主动去请求一次
     */
    UPAY_BILL_ORDER,

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
     */
    UPAY_MESSAGE,

    /**
     * 银行收款短信
     */
    BANK_SMS,

    /**
     * 获取设备对应的云闪付账号信息（账号，密码）
     */
    UPAY_ACCOUNT,


    /**
     * 运行状态报备，客户端对服务端所需的应用状态进行监听，当状态变化时要向服务端报备最新状态
     */
    RUNNING_STATE;


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
