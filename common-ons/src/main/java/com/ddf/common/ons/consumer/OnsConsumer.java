package com.ddf.common.ons.consumer;

/**
 * ONS消费者接口
 *
 * @author snowball
 * @date 2021/8/26 16:29
 **/
public interface OnsConsumer {

    /**
     * 获取并发消费者数量
     *
     * @return
     */
    String getConsumeThreadNums();

    /**
     * 获取GroupId
     * @return
     */
    String getGroupId();

    /**
     * 获取TOPIC
     * @return
     */
    String getTopic();

    /**
     * 获取消息路由表达式
     * @return
     */
    String getExpression();
    
}
