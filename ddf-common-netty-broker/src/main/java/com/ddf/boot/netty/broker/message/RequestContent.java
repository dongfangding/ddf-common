package com.ddf.boot.netty.broker.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * 通道传输数据格式定义类
 *
 * @author dongfang.ding
 * @date 2019/7/5 14:59
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestContent implements Serializable {
    /**
     * 扩展头每个键值对之间的分隔符，注意空格
     */
    private static final String SPLIT_LINE = "; ";
    /**
     * 扩展头键值对之间的分隔符，注意空格
     */
    private static final String SPLIT_KEY_VALUE = ": ";

    /**
     * 唯一标识此次请求，一个随机数
     */
    @JsonInclude
    private String requestId;

    /**
     * REQUEST 请求 RESPONSE 应答
     * @see Type
     */
    @JsonInclude
    private String type;
    /**
     * 本次请求要做什么事情,比如心跳包还是业务处理，不同的业务要做的事情不一样，处理主体数据格式也不一样
     */
    @JsonInclude
    private String cmd;

    /**
     * 请求时间
     */
    private Long requestTime;

    /**
     * 响应时间
     */
    private Long responseTime;

    /**
     * 主体数据
     */
    private String body;

    /**
     * 扩展字段
     * 类似http请求头，解析格式为key1: value1; key2: value2，注意是有空格的
     */
    private String extra;

    @JsonIgnore
    private transient Map<String, String> extraMap;

    public RequestContent() {

    }

    public RequestContent(String requestId, Type type, Cmd cmd, Long requestTime, String content) {
        this.requestId = requestId;
        this.type = type.name();
        this.cmd = cmd.name();
        this.requestTime = requestTime;
        this.body = content;
    }

    /**
     * 主动发起请求推送数据
     *
     * @param content
     * @return
     */
    public static RequestContent request(String content) {
        return new RequestContent(UUID.randomUUID().toString(), Type.REQUEST, Cmd.ECHO, System.currentTimeMillis(), content);
    }

    /**
     * 对收到的请求应答数据已收到
     *
     * @param requestContent
     * @return
     */
    public static RequestContent responseAccept(RequestContent requestContent) {
        return response(requestContent, "202");
    }

    /**
     * 对收到的请求应答业务处理成功
     *
     * @param requestContent
     * @return
     */
    public static RequestContent responseOK(RequestContent requestContent) {
        return response(requestContent, "200");
    }

    /**
     * 服务端向客户端发送心跳检测命令
     *
     * @return
     */
    public static RequestContent heart() {
        return new RequestContent(UUID.randomUUID().toString(), Type.REQUEST, Cmd.HEART, System.currentTimeMillis(), "ping");
    }

    /**
     * 添加扩展字段
     * @param key
     * @param value
     * @return
     */
    public RequestContent addExtra(String key, String value) {
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
     * @param requestContent
     * @return
     * @throws JsonProcessingException
     */
    public static String serial(RequestContent requestContent) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(requestContent);
    }


    /**
     * 根据请求数据构造响应数据
     * @param requestContent
     * @param code
     * @return
     */
    private static RequestContent response(RequestContent requestContent, String code) {
        RequestContent response = new RequestContent();
        response.setType(Type.RESPONSE.name());
        response.setRequestTime(requestContent.getRequestTime());
        response.setRequestId(requestContent.getRequestId());
        response.setCmd(requestContent.getCmd());
        response.setResponseTime(System.currentTimeMillis());
        response.setBody(code);
        return response;
    }

    /**
     * 解析扩展字段,注意空格
     */
    private RequestContent parseExtra() {
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

    public String getRequestId() {
        return requestId;
    }

    public RequestContent setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public String getType() {
        return type;
    }

    public RequestContent setType(String type) {
        this.type = type;
        return this;
    }

    public String getCmd() {
        return cmd;
    }

    public RequestContent setCmd(String cmd) {
        this.cmd = cmd;
        return this;
    }

    public Long getRequestTime() {
        return requestTime;
    }

    public RequestContent setRequestTime(Long requestTime) {
        this.requestTime = requestTime;
        return this;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public RequestContent setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
        return this;
    }

    public String getBody() {
        return body;
    }

    public RequestContent setBody(String body) {
        this.body = body;
        return this;
    }

    public String getExtra() {
        return extra;
    }


    /**
     * 由于set添加扩展值容易出错，因此不对外提供，进攻解码器使用
     * @param extra
     * @return
     */
    private RequestContent setExtra(String extra) {
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
        HEART,
        /**
         * 应答服务器
         */
        ECHO
    }
}
