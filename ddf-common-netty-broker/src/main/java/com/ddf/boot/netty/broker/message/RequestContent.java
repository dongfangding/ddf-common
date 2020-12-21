package com.ddf.boot.netty.broker.message;

import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.boot.common.core.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 通道传输数据格式定义类
 *
 * @author dongfang.ding
 * @date 2019/7/5 14:59
 */
@Data
@NoArgsConstructor
@ApiModel("报文类")
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
@Accessors(chain = true)
public class RequestContent<T> implements Serializable {
    /**
     * 扩展头每个键值对之间的分隔符，注意空格
     */
    private static final String SPLIT_LINE = "; ";
    /**
     * 扩展头键值对之间的分隔符，注意空格
     */
    private static final String SPLIT_KEY_VALUE = ": ";

    /**
     * 服务端发送标识
     */
    public static final Integer SEND_MODE_SERVER = 0;

    /**
     * 客户端发送标识
     */
    public static final Integer SEND_MODE_CLIENT = 1;

    /**
     * 唯一标识此次请求，一个随机数
     */
    private String requestId;

    /**
     * REQUEST 请求 RESPONSE 应答
     *
     * @see Type
     */
    private Type type;

    /**
     * 主动发送方身份
     * 0 服务端
     * 1 客户端
     */
    private Integer sendMode;

    /**
     * 响应码
     * 针对请求时该参数无效
     */
    private Integer code = 0;

    /**
     * 本次请求要做什么事情,比如心跳包还是业务处理，不同的业务要做的事情不一样，处理主体数据格式也不一样
     */
    private String cmd;

    /**
     * 如果相同的指令在客户端有多种工具，可以通过这个来指定通道
     * 比如都是发起支付，参数是一样的，可以通过这个字段来区分是支付宝还是微信，这只是一个例子，用于表明数据一致时，可通过该字段进行业务区分
     * 客户端应用通道
     */
    private String clientChannel;

    /**
     * 报文发送时间
     */
    private Long timestamp;

    /**
     * 主体数据
     */
    private T body;

    /**
     * 扩展字段
     * 类似http请求头，解析格式为key1: value1; key2: value2，注意是有空格的
     */
    private String extra;

    @JsonIgnore
    private transient Map<String, String> extraMap;


    public RequestContent(String requestId, Type type, String cmd, Integer sendMode, String clientChannel,
            Long timestamp, T content) {
        this.requestId = requestId;
        this.type = type;
        this.cmd = cmd;
        this.sendMode = sendMode;
        this.clientChannel = clientChannel;
        this.timestamp = timestamp;
        this.body = content;
    }

    /**
     * 主动发起请求推送数据
     *
     * @param cmd     指令碼
     * @param content 发送的内容
     * @param <T>     内容类型
     * @return
     */
    public static <T> RequestContent<T> request(String cmd, T content) {
        return new RequestContent<>(IdsUtil.getNextStrId(), Type.REQUEST, cmd, SEND_MODE_SERVER, null,
                System.currentTimeMillis(), content
        );
    }

    /**
     * 对收到的请求应答数据已收到
     *
     * @param requestContent 收到的数据
     * @param <T>            数据body类型
     * @return
     */
    public static <T> RequestContent<T> responseAccept(RequestContent<T> requestContent) {
        return response(requestContent, ResponseCodeEnum.CODE_RECEIVED.getCode(), null);
    }

    /**
     * 对收到的请求应答业务处理成功同时返回给客户端数据
     *
     * @param requestContent 收到的数据
     * @param data           要返回的数据
     * @param <T>            收到的数据body类型
     * @param <R>            返回的body数据内容
     * @return
     */
    public static <T, R> RequestContent<R> responseSuccess(RequestContent<T> requestContent, R data) {
        return response(requestContent, ResponseCodeEnum.CODE_COMPLETE.getCode(), data);
    }

    /**
     * 对收到的请求应答业务处理成功同时返回给客户端数据
     *
     * @param requestContent 收到的数据
     * @param <T>            收到的数据body类型
     * @return
     */
    public static <T> RequestContent<T> responseSuccess(RequestContent<T> requestContent) {
        return response(requestContent, ResponseCodeEnum.CODE_COMPLETE.getCode(), null);
    }

    /**
     * 服务端向客户端发送心跳检测命令
     *
     * @return
     */
    public static <T> RequestContent<T> heart() {
        return request(Cmd.PING.name(), null);
    }

    /**
     * 添加扩展字段
     *
     * @param key   扩展字段key
     * @param value 扩展字典value
     * @return
     */
    public RequestContent<T> addExtra(String key, String value) {
        if (extra == null) {
            extra = "";
        } else if (extra.length() > 0) {
            extra += SPLIT_LINE;
        }
        extra += key + SPLIT_KEY_VALUE + value;
        parseExtra();
        return this;
    }

    /**
     * 序列化RequestContent
     *
     * @return
     */
    public String serial() {
        return JsonUtil.asString(this);
    }


    /**
     * 根据请求数据构造响应数据
     *
     * @param requestContent 收到的数据
     * @param code           响应code码
     * @param data           响应的数据
     * @param <T>            收到的数据内容类型
     * @param <R>            返回的数据内容类型
     * @return
     */
    private static <T, R> RequestContent<R> response(RequestContent<T> requestContent, Integer code, R data) {
        RequestContent<R> response = new RequestContent<>();
        response.setRequestId(requestContent.getRequestId());
        response.setType(Type.RESPONSE);
        response.setCode(code);
        response.setCmd(requestContent.getCmd());
        response.setTimestamp(System.currentTimeMillis());
        response.setClientChannel(requestContent.getClientChannel());
        response.setSendMode(SEND_MODE_SERVER);
        response.setBody(data);
        return response;
    }

    /**
     * 解析扩展字段,注意空格
     */
    private RequestContent<T> parseExtra() {
        if (null != extra && !"".equals(extra)) {
            try {
                String[] keyValueArr = extra.split(SPLIT_LINE);
                if (keyValueArr.length > 0) {
                    Map<String, String> extraMap = getExtraMap() == null ? new HashMap<>(16) : getExtraMap();
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
     * 由于set添加扩展值容易出错，因此不对外提供，进攻解码器使用
     *
     * @param extra
     * @return
     */
    private RequestContent<T> setExtra(String extra) {
        this.extra = extra;
        return parseExtra();
    }

    public Map<String, String> getExtraMap() {
        return extraMap;
    }

    public void setExtraMap(Map<String, String> extraMap) {
        this.extraMap = extraMap;
    }

    /**
     * 连接请求类型
     */
    public enum Type {
        /**
         * 请求
         */
        REQUEST,
        /**
         * 响应
         */
        RESPONSE
    }


    /**
     * 命名
     */
    public enum Cmd {
        /**
         * 心跳检测
         */
        PING,
        /**
         * 应答服务器
         */
        ECHO
    }
}
