package com.ddf.boot.common.stomp.model.dto;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * <p>协议层发送的数据，即客户端收到的数据结构</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/03/19 18:24
 */
@Data
public class StompMessageProtocol implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息代码
     */
    private String messageCode;

    /**
     * 自定义数据, 这个对应着发送端请求时的body对象， 不能用泛型，这是底层发送数据， 用泛型会带来反序列化问题
     */
    private String data;
}
