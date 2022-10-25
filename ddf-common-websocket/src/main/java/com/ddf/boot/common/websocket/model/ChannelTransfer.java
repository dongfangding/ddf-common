package com.ddf.boot.common.websocket.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 通道传输报文日志记录
 *
 * @author dongfang.ding
 */
@TableName(value = "log_channel_transfer")
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChannelTransfer {

    /**
     * 服务端主动发送数据表示
     */
    public static final Integer SEND_FLAG_SERVER = 0;
    /**
     * 客户端主动发送数据标识
     */
    public static final Integer SEND_FLAG_CLIENT = 1;

    /**
     * 已发送
     */
    public static final Integer STATUS_SEND = 0;
    /**
     * 已接收
     */
    public static final Integer STATUS_RECEIVED = 1;
    /**
     * 已响应
     */
    public static final Integer STATUS_RESPONSE = 2;
    /**
     * 已处理
     */
    public static final Integer STATUS_SUCCESS = 3;
    /**
     * 处理失败
     */
    public static final Integer STATUS_FAILURE = 4;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 请求id，用来响应和判断客户端是否重复请求
     * ;如果消息解析失败那个这个值为空，照样保存，以便排错
     */
    private String requestId;

    /**
     * 认证身份的唯一标识符，如用户id
     */
    private String accessKeyId;

    /**
     * 认证身份的名称
     */
    private String accessKeyName;

    /**
     * 客户端类型
     * @see com.ddf.boot.quick.websocket.model.AuthPrincipal.LoginType
     */
    private String loginType;

    /**
     * 指令码
     */
    private String cmd;

    /**
     * 认证时的授权码
     */
    private String authCode;

    /**
     * 客户端通道
     */
    private String clientChannel;

    /**
     * 服务端地址
     */
    private String serverAddress;

    /**
     * 客户端地址
     */
    private String clientAddress;

    /**
     * 指令下发人
     */
    private String operatorId;

    /**
     * 业务主键，服务端根据这个值和cmd判断客户端是否对同一条业务数据发送相同的命令码
     */
    private String logicPrimaryKey;

    /**
     * 主动发送方标识 0 服务端  1 客户端
     */
    private Integer sendFlag;

    /**
     * 服务端用来存储发送时携带的业务主键，方便数据回传时，不依赖于客户端将业务数据回传
     */
    private String businessData;

    /**
     * 传输内容
     */
    private String request;

    /**
     * 响应内容
     */
    private String response;

    /**
     * 0 已发送 1 已接收 2 已响应 3 已处理 4 处理失败
     */
    private Integer status;

    /**
     * 失败原因
     */
    private String errorMessage;

    /**
     * 交互的完整报文，使用json数组包裹原始报文
     */
    private String fullRequestResponse;

    /**
     * 创建时间
     */
    private Date createTime;
}
