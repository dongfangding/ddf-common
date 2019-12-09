package com.ddf.boot.common.websocket.model.ws;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报文类夹杂业务数据，
 * 适用于处理类如果需要返回自定义报文数据，并且还需要返回业务数据
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageWrapper<T, S> {

    private Message<T> message;

    private S businessData;

    /**
     * 无业务数据
     * @param message
     * @param <T>
     * @return
     */
    public static <T> MessageWrapper<T, ?> noBusiness(Message<T> message) {
        return new MessageWrapper<>(message, null);
    }


    /**
     * 携带业务数据
     * @param message
     * @param businessData
     * @param <T>
     * @param <S>
     * @return
     */
    public static <T, S> MessageWrapper<T, S> withBusiness(Message<T> message, S businessData) {
        return new MessageWrapper<>(message, businessData);
    }
}
