package com.ddf.common.ons.enume;

/**
 * 消费模式
 *
 * @author snowball
 * @date 2021/8/26 14:18
 **/
public enum ConsumeMode {

    /**
     * 串行接收异步消息
     */
    SERIAL,

    /**
     * 批量接收异步传递的消息
     */
    BATCH,

    /**
     * 有序接收异步传递的消息。一个队列，一个线程
     */
    ORDERLY
}
