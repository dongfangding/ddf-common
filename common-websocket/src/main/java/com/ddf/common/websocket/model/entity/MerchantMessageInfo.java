package com.ddf.common.websocket.model.entity;

import com.ddf.common.entity.BaseDomain;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 云闪付收款到账消息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MerchantMessageInfo extends BaseDomain implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息来源，默认未知，即未解析成功，无法判别数据
     */
    public static final Byte SOURCE_TYPE_UNKNOWN = 0;
    /**
     * 消息来源，云闪付普通码到账消息
     */
    public static final Byte SOURCE_TYPE_NORMAL_INCOME_UPAY_MESSAGE = 1;
    /**
     * 消息来源，云闪付普通码入账账单记录
     */
    public static final Byte SOURCE_TYPE_NORMAL_INCOME_UPAY_BILL_ORDER = 2;
    /**
     * 消息来源，银行收入短信
     */
    public static final Byte SOURCE_TYPE_INCOME_BANK_SMS = 3;
    /**
     * 消息来源，垃圾短信
     */
    public static final Byte SOURCE_TYPE_GARBAGE_SMS = 4;
    /**
     * 消息来源，云闪付登录验证码短信
     */
    public static final Byte SOURCE_TYPE_UNION_PAY_LOGIN_VERIFY_CODE = 5;
    /**
     * 消息来源，银行支出短信
     */
    public static final Byte SOURCE_TYPE_PAY_BANK_SMS = 6;
    /**
     * 消息来源，云闪付支出消息
     */
    public static final Byte SOURCE_TYPE_PAY_UPAY_MESSAGE = 7;
    /**
     * 消息来源，云闪付转账支出交易记录
     */
    public static final Byte SOURCE_TYPE_PAY_UPAY_BILL_ORDER = 8;

    /**
     * 消息来源，云闪付商户码到账消息
     */
    public static final Byte SOURCE_TYPE_MERCHANT_INCOME_UPAY_MESSAGE = 9;

    /**
     * 消息来源，云闪付商户码入账账单记录
     */
    public static final Byte SOURCE_TYPE_MERCHANT_INCOME_UPAY_BILL_ORDER = 10;

    /**
     * 消息来源，云闪付转账安全认证短信验证码
     */
    public static final Byte SOURCE_TYPE_UNION_PAY_VERIFY_CODE = 11;


    /**
     * 状态 未处理
     */
    public static final Byte STATUS_NOT_DEAL = 0;
    /**
     * 状态 处理成功
     */
    public static final Byte STATUS_SUCCESS = 1;
    /**
     * 状态 模板未匹配
     */
    public static final Byte STATUS_NOT_MATCH_TEMPLATE = 2;
    /**
     * 状态 业务处理错误
     */
    public static final Byte STATUS_LOGIC_ERROR = 3;
    /**
     * 状态 未匹配订单
     */
    public static final Byte STATUS_NOT_MATCH_ORDER = 4;
    /**
     * 状态 数据格式有误
     */
    public static final Byte STATUS_DATA_INVALID = 5;
    /**
     * 状态 订单重复匹配，三种支付渠道每一个只要匹配到订单就要记录到订单的id，但是只有一个是真正触发更新的。所以需要一个状态区分出来
     */
    public static final Byte STATUS_ORDER_REPEAT_MATCH = 6;

    @ApiModelProperty(value = "商户id")
    private String merchantId;

    @ApiModelProperty(value = "报文中的request_id")
    private String requestId;

    /**
     * 设备id
     */
    @ApiModelProperty(value = "设备id")
    private String deviceId;

    @ApiModelProperty(value = "设备号")
    private String deviceNumber;

    @ApiModelProperty("哪个指令码收到的数据")
    private String cmd;

    @ApiModelProperty(value = "报文中接收的body主体数据,如果报文中有多个数据，对应多条记录，分开存储")
    private String singleMessagePayload;

    /**
     * 消息内容
     */
    @ApiModelProperty(value = "对报文中的内容进行字符串拼接解释")
    private String description;

    /**
     * @see MerchantMessageInfo#STATUS_NOT_DEAL
     */
    @ApiModelProperty(value = "处理状态 0 未处理 1 处理成功 2 模板未匹配 3 业务处理错误 4 未匹配订单 5 数据格式有误 6 订单重复匹配")
    private Byte status;

    /**
     * @see MerchantMessageInfo#SOURCE_TYPE_UNKNOWN
     */
    @ApiModelProperty("数据来源，用以区分数据的具体用途。如短信，会分为收入、支出短信，验证码短信等")
    private Byte sourceType;

    @ApiModelProperty(value = "处理错误原因,只取异常的getMessage,给前端展示用")
    private String errorMessage;

    @ApiModelProperty(value = "取异常栈信息，方便查错")
    private String errorStack;

    @ApiModelProperty("云闪付id或短信id等客户端发送数据的唯一标识符,用来做唯一校验，避免重复插入")
    private String tradeNo;

    @ApiModelProperty("订单时间或收件时间")
    private Date receiveTime;

    @ApiModelProperty("自己系统的订单id,用以维系该表记录匹配到了哪条订单。merchant_order_info的id")
    private String orderId;

}
