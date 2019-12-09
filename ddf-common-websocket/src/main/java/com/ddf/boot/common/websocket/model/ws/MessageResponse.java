package com.ddf.boot.common.websocket.model.ws;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 指令发送后服务端给调用方的响应结果
 * 
 * @author dongfang.ding
 * @date 2019/9/24 16:18 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse<T> implements Serializable {

    /** 响应数据延迟 */
    public static final Integer SERVER_CODE_RECEIVED = 202;

    /** 服务端响应状态码 ，已处理完成 */
    public static final Integer SERVER_CODE_COMPLETE = 200;

    /** 服务端响应状态码， 客户端未登录*/
    public static final Integer SERVER_CODE_NOT_LOGIN = -2;

    /** 服务端响应状态码， 处理失败*/
    public static final Integer SERVER_CODE_ERROR = 500;

    /** 服务端响应客户端请求资源不存在，如想要获取最新版本，服务端无可用版本等 */
    public static final Integer SERVER_CODE_RESOURCE_NOT_EXIST = 404;

    /** 客户端格式无效 */
    public static final Integer SERVER_CODE_FORMAT_INVALID = 422;

    /** 客户端针对同一个业务主键数据重复下达指令 */
    public static final Integer SERVER_CODE_CLIENT_REPEAT_REQUEST = 400;


    private static final MessageResponse NONE = new MessageResponse();

    /**
     * 响应码
     */
    private Integer code;

    private String message;

    /**
     * 不需要调用方处理这个参数
     * <p>
     * 请求的id，针对执行下发的一个阻塞实现，如果请求被转发到另一台服务器，另一台服务器返回的数据需要携带
     * requestId，这样数据回传回来才能找到数据对应的源请求
     */
    private String requestId;

    /**
     * 响应的数据
     */
    private T payload;

    public MessageResponse(Integer code, String message, T payload) {
        this.code = code;
        this.message = message;
        this.payload = payload;
    }

    /**
     * 代表没有返回值的空对象
     * @return
     */
    public static MessageResponse none() {
        return NONE;
    }


    public static MessageResponse success() {
        return new MessageResponse<>(SERVER_CODE_COMPLETE, "操作成功", null);
    }

    public static <T> MessageResponse<T> success(T payload) {
        return new MessageResponse<>(SERVER_CODE_COMPLETE, "操作成功", payload);
    }

    public static MessageResponse<String> failure(Integer code, String message) {
        return new MessageResponse<>(code, message, null);
    }

    public static MessageResponse<String> failure(String message) {
        return new MessageResponse<>(SERVER_CODE_ERROR, message, null);
    }

    public static <T> MessageResponse<T> failure(String message, T payload) {
        return new MessageResponse<>(SERVER_CODE_ERROR, message, payload);
    }


    /**
     * 阻塞实现如果超时的一个提示方法
     * @return
     */
    public static MessageResponse delay(String requestId) {
        return new MessageResponse<>(SERVER_CODE_RECEIVED, "【" + requestId + "】指令已经下发，请稍后确认结果！", null);
    }

    /**
     * 异步提示方法
     * @return
     */
    public static MessageResponse confirm() {
        return new MessageResponse<>(SERVER_CODE_COMPLETE, "下发指令成功，请稍后确认结果！", null);
    }

    /**
     * 客户端针对同一个业务主键进行重复请求
     * @return
     */
    public static MessageResponse repeatRequest() {
        return new MessageResponse<>(SERVER_CODE_CLIENT_REPEAT_REQUEST, "重复请求！", null);
    }

}
