package com.ddf.boot.common.stomp.model.req;

import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2026/09/08 12:11
 */
@Data
public class StompMessageRequest<T> {

    /**
     * 发往的目的地
     */
    private String topic;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息代码
     */
    private String messageCode;

    /**
     * 业务层自己组装的对象
     */
    private T data;
}
