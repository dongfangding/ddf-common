package com.ddf.boot.common.websocket.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * 通道传输报文日志记录
 *
 * @author dongfang.ding
 *
 */
@TableName(value = "log_channel_transfer")
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@ApiModel("通道传输报文日志记录")
public class ChannelTransfer {

    /** 服务端主动发送数据表示 */
    public static final Integer SEND_FLAG_SERVER = 0;
    /** 客户端主动发送数据标识 */
    public static final Integer SEND_FLAG_CLIENT = 1;

    /** 已发送 */
    public static final Integer STATUS_SEND = 0;
    /** 已接收 */
    public static final Integer STATUS_RECEIVED = 1;
    /** 已响应 */
    public static final Integer STATUS_RESPONSE = 2;
    /** 已处理 */
    public static final Integer STATUS_SUCCESS = 3;
    /** 处理失败 */
    public static final Integer STATUS_FAILURE = 4;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * ;如果消息解析失败那个这个值为空，照样保存，以便排错
     */
    @ApiModelProperty("请求id，用来响应和判断客户端是否重复请求")
    private String requestId;

    @ApiModelProperty("认证身份的唯一标识符，如用户id")
    private String accessKeyId;

    @ApiModelProperty("认证身份的名称")
    private String accessKeyName;

    /**
     * @see com.ddf.boot.quick.websocket.model.AuthPrincipal.LoginType
     */
    @ApiModelProperty("客户端类型")
    private String loginType;

    @ApiModelProperty("指令码")
    private String cmd;

    @ApiModelProperty("认证时的授权码")
    private String authCode;

    @ApiModelProperty("客户端通道")
    private String clientChannel;

    @ApiModelProperty("服务端地址")
    private String serverAddress;

    @ApiModelProperty("客户端地址")
    private String clientAddress;

    @ApiModelProperty("指令下发人")
    private String operatorId;

    @ApiModelProperty("业务主键，服务端根据这个值和cmd判断客户端是否对同一条业务数据发送相同的命令码")
    private String logicPrimaryKey;

    @ApiModelProperty("主动发送方标识 0 服务端  1 客户端")
    private Integer sendFlag;

    @ApiModelProperty("服务端用来存储发送时携带的业务主键，方便数据回传时，不依赖于客户端将业务数据回传")
    private String businessData;

    @ApiModelProperty("传输内容")
    private String request;

    @ApiModelProperty("响应内容")
    private String response;

    @ApiModelProperty("0 已发送 1 已接收 2 已响应 3 已处理 4 处理失败")
    private Integer status;

    @ApiModelProperty("失败原因")
    private String errorMessage;

    @ApiModelProperty("交互的完整报文，使用json数组包裹原始报文")
    private String fullRequestResponse;

    @ApiModelProperty("创建时间")
    private Date createTime;
}