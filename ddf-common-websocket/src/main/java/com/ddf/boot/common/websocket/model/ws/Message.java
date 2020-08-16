package com.ddf.boot.common.websocket.model.ws;

import com.ddf.boot.common.core.exception.GlobalCustomizeException;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.core.util.StringUtil;
import com.ddf.boot.common.core.util.SecureUtil;
import com.ddf.boot.common.websocket.enumerate.CmdEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 报文内容类
 * 1. {"type":"REQUEST","requestId":"1566439043767dd92a290ea144f9dbcc","cmd":"QRCODE_CREATE","code":0,"extra":null,"body":{"amount":100,"type":"upay","orderId":"133","bankCardNumber":"6212264589887566599"}}
 * 2.
 *
 *
 * @author dongfang.ding
 * @date 2019/12/21
 */
@Data
@NoArgsConstructor
@ApiModel("报文类")
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class Message<T> {

    /**
     * 扩展头每个键值对之间的分隔符，注意空格
     */
    private transient static final String SPLIT_LINE = "; ";
    /**
     * 扩展头键值对之间的分隔符，注意空格
     */
    private transient static final String SPLIT_KEY_VALUE = ": ";

    private transient static final String SEND_MODEL_SERVER = "SERVER";

    public transient static final String ID_CARD_NO = "idCardNo";
    public transient static final String MOBILE = "mobile";
    public transient static final String ACCOUNT = "account";
    public transient static final String LOGIN_PASSWORD = "loginPassword";
    public transient static final String PAY_PASSWORD = "payPassword";

    @ApiModelProperty(value = "标识请求还是响应", allowableValues = "REQUEST, RESPONSE")
    private Type type;

    @ApiModelProperty("请求唯一标识符")
    private String requestId;

    @ApiModelProperty(value = "指令", allowableValues = "ECHO, RESTART, UPGRADE, SWITCH_IP, GPS, QRCODE_CREATE, UPAY_MESSAGE")
    private CmdEnum cmd;

    @ApiModelProperty("响应码")
    private Integer code = 0;

    @ApiModelProperty(value = "扩展字段")
    private String extra;

    @ApiModelProperty("业务主键的唯一id")
    private String logicPrimaryKey;

    @ApiModelProperty("客户端应用通道")
    private ClientChannel clientChannel;

    /**
     * 发送方标识
     */
    @ApiModelProperty(value = "发送放标识", allowableValues = "server, client")
    private String sendModel;

    /**
     * 请求与响应时间
     */
    private Long timestamp;

    /**
     * 解析请求头存放的对象，非报文传输字段
     */
    @JsonIgnore
    private transient Map<String, String> extraMap;

    @ApiModelProperty(value = "主体数据内容")
    private T body;

    public Message(Type type, String requestId, String sendModel, CmdEnum cmd, T body, ClientChannel clientChannel) {
        this.type = type;
        this.requestId = requestId;
        this.sendModel = sendModel;
        this.cmd = cmd;
        this.body = body;
        this.timestamp = System.currentTimeMillis();
        this.clientChannel = clientChannel;
    }

    public Message(Type type, String requestId, String sendModel, CmdEnum cmd, T body, Integer code, ClientChannel clientChannel) {
        this.type = type;
        this.requestId = requestId;
        this.sendModel = sendModel;
        this.cmd = cmd;
        this.code = code;
        this.body = body;
        this.timestamp = System.currentTimeMillis();
        this.clientChannel = clientChannel;
    }

    public enum Type {
        /**
         * 标识请求
         */
        REQUEST,
        /**
         * 标识响应
         */
        RESPONSE
    }

    /**
     * 客户端连接上来的欢迎语
     * @param payload
     * @return
     */
    public static Message<String> echo(String payload) {
        return new Message<>(Type.RESPONSE, StringUtil.randomString(64), SEND_MODEL_SERVER, CmdEnum.PONG,
                payload, ClientChannel.UPAY);
    }

    /**
     * 服务端响应客户端未登录
     * @return
     */
    public static Message<String> responseNotLogin(@NotNull WebSocketMessage<?> message) {
        if (message == null) {
            return null;
        }
        String payload = (String) message.getPayload();
        Message message1 = JsonUtil.toBean(payload, Message.class);
        return new Message<>(Type.RESPONSE, message1.getRequestId(), SEND_MODEL_SERVER, message1.getCmd(), "未登录",
                MessageResponse.SERVER_CODE_NOT_LOGIN, ClientChannel.UPAY);
    }

    /**
     * 服务端响应客户端的数据在服务端没有对应的请求
     * @return
     */
    public static Message<String> responseNotMatchRequest(@NotNull Message message) {
        if (message == null) {
            return null;
        }
        return buildResponseMessage(message, "没有找到对应的请求", MessageResponse.SERVER_CODE_ERROR);
    }


    /**
     * 服务端响应客户端在重复请求数据
     * @return
     */
    public static Message<String> responseRepeatRequest(@NotNull Message message) {
        if (message == null) {
            return null;
        }
        return buildResponseMessage(message, "重复请求", MessageResponse.SERVER_CODE_ERROR);
    }

    /**
     * 将Message对象包装成发送的TextMessage
     * @param message
     * @return
     */
    public static TextMessage wrapper(@NotNull Message message) {
        if (message == null) {
            return null;
        }
        return new TextMessage(JsonUtil.asString(message));
    }


    /**
     * 加密加签封装
     * @param message
     * @return
     */
    public static TextMessage wrapperWithSign(@NotNull Message message) {
        if (message == null) {
            return null;
        }
        String body = JsonUtil.asString(message.getBody());
        String sign = SecureUtil.signWithHMac(body, message.getCmd().name());
        message.addExtra("sign", sign);
        return new TextMessage(SecureUtil.privateEncryptBcd(JsonUtil.asString(message)));
    }

    /**
     * 服务端请求数据
     * @param cmd
     * @param body
     * @return
     */
    public static <T> Message<T> request(CmdEnum cmd, T body) {
        return new Message<>(Message.Type.REQUEST, StringUtil.randomString(64), SEND_MODEL_SERVER, cmd, body, ClientChannel.UPAY);
    }

    /**
     * 服务端请求数据
     * @param cmd
     * @param body
     * @return
     */
    public static <T> Message<T> request(CmdEnum cmd, T body, ClientChannel clientChannel) {
        return new Message<>(Message.Type.REQUEST, StringUtil.randomString(64), SEND_MODEL_SERVER, cmd, body, clientChannel);
    }

    /**
     * 响应客户端数据
     * @param message
     * @param <T>
     * @return
     */
    public static <T> Message<T> responseReceived(Message message, T body, Integer code) {
        if (message != null) {
            return buildResponseMessage(message, body, code);
        }
        return null;
    }

    /**
     * 通用响应客户端数据处理成功
     * @param message
     * @return
     */
    public static Message<String> responseSuccess(Message<?> message) {
        return responseReceived(message, "处理成功", MessageResponse.SERVER_CODE_COMPLETE);
    }

    /**
     * 根据客户端数据构建响应数据
     * @param message
     * @param body
     * @param code
     * @param <T>
     * @return
     */
    public static <T> Message<T> buildResponseMessage(Message<?> message, T body, Integer code) {
        if (message == null) {
            return null;
        }
        return new Message<>(Type.RESPONSE, message.getRequestId(), SEND_MODEL_SERVER, message.getCmd(), body,
                code, message.getClientChannel());
    }

    /**
     * 添加扩展字段
     *
     * @param key
     * @param value
     * @return
     */
    public Message addExtra(String key, String value) {
        if (extra == null) {
            extra = "";
        } else if (extra.length() > 0) {
            extra += SPLIT_LINE;
        }
        extra += key + SPLIT_KEY_VALUE + value;
        parseExtra();
        return this;
    }


    public void setExtra(String extra) {
        this.extra = extra;
        parseExtra();
    }

    /**
     * 解析扩展字段,注意空格
     */
    private Message parseExtra() {
        if (null != extra && !"".equals(extra)) {
            try {
                String[] keyValueArr = extra.split(SPLIT_LINE);
                if (keyValueArr.length > 0) {
                    Map<String, String> extraMap = getExtraMap() == null ? new HashMap<>() : getExtraMap();
                    String[] keyValue;
                    for (String s : keyValueArr) {
                        keyValue = s.split(SPLIT_KEY_VALUE);
                        extraMap.put(keyValue[0], keyValue[1]);
                    }
                    setExtraMap(extraMap);
                }
            } catch (Exception e) {
                extraMap = null;
            }
        }
        return this;
    }


    /**
     * 对报文数据进行解密与验签
     * @param textMessagePayload
     * @return
     */
    public static Message unSign(String textMessagePayload) {
        log.debug("待解密数据: {}", textMessagePayload);
        if (StringUtils.isBlank(textMessagePayload)) {
            return null;
        }
        String decrypt = SecureUtil.privateDecryptFromBcd(textMessagePayload);
        log.debug("解密后数据: {}", decrypt);
        Message<?> message = JsonUtil.toBean(decrypt, Message.class);
        String signStr = message.getExtraMap().get("sign");
        log.debug("报文中加签值: {}", signStr);
        String dataSign = SecureUtil.signWithHMac(JsonUtil.asString(message.getBody()), message.getCmd().name());
        log.debug("对数据解密后重新加签: {}", dataSign);
        if (!Objects.equals(signStr, dataSign)) {
            log.error("验签不通过！！报文中加签值: {}, 实际加签值: {}", signStr, dataSign);
            return null;
        }
        log.debug("验签通过");
        if (message.getClientChannel() == null) {
            message.setClientChannel(ClientChannel.UPAY);
        }
        return message;
    }

    /**
     * 将收到的消息转换为Message对象
     * @param textMessage
     * @return
     */
    public static Message toMessage(TextMessage textMessage) {
        String payload = textMessage.getPayload();
        Message message = JsonUtil.toBean(payload, Message.class);
        if (message.getClientChannel() == null) {
            throw new GlobalCustomizeException("请必须传入客户端应用类型");
        }
        return message;
    }


    /**
     * 在请求头上附加账号信息
     * @param idCardNo
     * @param mobile
     * @param account
     * @param loginPassword
     * @param payPassword
     */
    public void addAccount(String idCardNo, String mobile, String account, String loginPassword, String payPassword) {
        this.addExtra(ID_CARD_NO, idCardNo).addExtra(MOBILE, mobile).addExtra(ACCOUNT, account)
                .addExtra(LOGIN_PASSWORD, loginPassword).addExtra(PAY_PASSWORD, payPassword);
    }
}
