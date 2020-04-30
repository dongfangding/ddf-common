package com.ddf.boot.common.websocket.model.entity;

import com.ddf.boot.common.entity.BaseDomain;
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
    public static final Integer SOURCE_TYPE_UNKNOWN = 0;
    /**
     * 消息来源，支付方式应用普通码到账消息
     */
    public static final Integer SOURCE_TYPE_NORMAL_INCOME_TOPIC_MESSAGE = 1;
    /**
     * 消息来源，支付方式应用普通码入账账单记录
     */
    public static final Integer SOURCE_TYPE_NORMAL_INCOME_BILL_MATCH_ORDER = 2;
    /**
     * 消息来源，银行收入短信
     */
    public static final Integer SOURCE_TYPE_INCOME_BANK_SMS = 3;
    /**
     * 消息来源，垃圾短信
     */
    public static final Integer SOURCE_TYPE_GARBAGE_SMS = 4;
    /**
     * 消息来源，登录验证码短信
     */
    public static final Integer SOURCE_TYPE_LOGIN_VERIFY_CODE = 5;
    /**
     * 消息来源，银行支出短信
     */
    public static final Integer SOURCE_TYPE_PAY_BANK_SMS = 6;
    /**
     * 消息来源，支付方式应用支出消息
     */
    public static final Integer SOURCE_TYPE_PAY_TOPIC_MESSAGE = 7;
    /**
     * 消息来源，支付方式转账支出交易记录
     */
    public static final Integer SOURCE_TYPE_PAY_BILL_MATCH_ORDER = 8;

    /**
     * 消息来源，支付方式商户码到账消息
     */
    public static final Integer SOURCE_TYPE_MERCHANT_INCOME_TOPIC_MESSAGE = 9;

    /**
     * 消息来源，支付方式商户码入账账单记录
     */
    public static final Integer SOURCE_TYPE_MERCHANT_INCOME_BILL_MATCH_ORDER = 10;

    /**
     * 消息来源，支付方式转账安全认证短信验证码
     */
    public static final Integer SOURCE_TYPE_PAY_VERIFY_CODE = 11;

    /**
     * 消息来源，安全认证短信
     */
    public static final Integer SOURCE_TYPE_SAFETY_CERTIFICATION_SMS = 12;

    /**
     * 消息为忽略类型的数据，如云闪付每笔交易都会固定发送一个消息，但这个消息却无法分辨是收入还是支出；因此不能作为支付模板使用，
     * 但是如果不配置模板，就一直报错模板不匹配。因此需要配置一个特殊模板
     */
    public static final Integer SOURCE_TYPE_IGNORE_MESSAGE = 13;

    /**
     * 收到运营商发送的需要确认回复短信的类型，这里为求通用，没有具体细分到底是因为身边么要回复短信
     */
    public static final Integer SOURCE_TYPE_CONFIRM_SMS = 14;


    /**
     * 注册验证码
     */
    public static final Integer SOURCE_TYPE_REGISTRY_VERIFY_CODE = 15;


    /**
     * 状态 未处理
     */
    public static final Integer STATUS_NOT_DEAL = 0;
    /**
     * 状态 处理成功
     */
    public static final Integer STATUS_SUCCESS = 1;
    /**
     * 状态 模板未匹配
     */
    public static final Integer STATUS_NOT_MATCH_TEMPLATE = 2;
    /**
     * 状态 业务处理错误
     */
    public static final Integer STATUS_LOGIC_ERROR = 3;
    /**
     * 状态 未匹配订单
     */
    public static final Integer STATUS_NOT_MATCH_ORDER = 4;
    /**
     * 状态 数据格式有误
     */
    public static final Integer STATUS_DATA_INVALID = 5;
    /**
     * 状态 订单重复匹配，三种支付渠道每一个只要匹配到订单就要记录到订单的id，但是只有一个是真正触发更新的。所以需要一个状态区分出来
     */
    public static final Integer STATUS_ORDER_REPEAT_MATCH = 6;

    /**
     * 伪造认证方式（如短信发件方和系统配置不一致）
     */
    public static final Integer STATUS_ERROR_CREDIT = 7;


    /**
     * 收款订单
     */
    public static final Integer ORDER_TYPE_RECEIVE = 1;
    /**
     * 付款订单
     */
    public static final Integer ORDER_TYPE_PAYMENT = 2;


    /**
     * 默认匹配人相关信息
     */
    public static final String MATCH_BY_ID_DEFAULT = "System";
    public static final String MATCH_BY_NAME_DEFAULT = "系统";




    @ApiModelProperty(value = "商户id")
    private String merchantId;

    @ApiModelProperty("商户名")
    private String merchantName;

    @ApiModelProperty(value = "报文中的request_id")
    private String requestId;

    /**
     * 设备id
     */
    @ApiModelProperty(value = "设备id")
    private Long deviceId;

    @ApiModelProperty(value = "设备号")
    private String deviceNumber;

    @ApiModelProperty("指令想要在哪个应用上执行\\r\\n1. UPAY 云闪付\\r\\n2. ALIPAY 支付宝\\r\\n3. WECHAT_PAY 微信\\r\\n4. ICBC_APP 工行app\\r\\n5. CCB_APP 建行app")
    private String clientChannel;

    @ApiModelProperty(value = "设备序列号")
    private String sequence;

    @ApiModelProperty("哪个指令码收到的数据")
    private String cmd;

    @ApiModelProperty(value = "报文中接收的body主体数据,如果报文中有多个数据，对应多条记录，分开存储")
    private String singleMessagePayload;

    @ApiModelProperty("消息的解析内容json串，由于有一些数据需要解析，这里将解析后的数据放进去，方便直接取用")
    private String parseContent;

    @ApiModelProperty("账单交易时间")
    private Date billTime;

    /**
     * 消息内容
     */
    @ApiModelProperty(value = "对报文中的内容进行字符串拼接解释")
    private String description;

    /**
     * @see MerchantMessageInfo#STATUS_NOT_DEAL
     */
    @ApiModelProperty(value = "处理状态 0 未处理 1 处理成功 2 模板未匹配 3 业务处理错误 4 未匹配订单 5 数据格式有误 6 订单重复匹配")
    private Integer status;

    /**
     * @see MerchantMessageInfo#SOURCE_TYPE_UNKNOWN
     */
    @ApiModelProperty("数据来源，用以区分数据的具体用途。如短信，会分为收入、支出短信，验证码短信等")
    private Integer sourceType;

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

    @ApiModelProperty("订单类型 0 数据错误 1 收款 2 转账")
    private Integer orderType;

    @ApiModelProperty("匹配完成人，如果是系统匹配的则为0，否则为操作人的id")
    private String matchById;

    @ApiModelProperty("匹配完成人，如果是系统匹配的则为System，否则为操作人的name")
    private String matchByName;

}
