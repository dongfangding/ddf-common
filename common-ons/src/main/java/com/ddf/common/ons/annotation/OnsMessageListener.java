package com.ddf.common.ons.annotation;

import com.aliyun.openservices.ons.api.ExpressionType;
import com.ddf.common.ons.enume.MessageModel;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ONS消息监听器注解
 *
 * @author snowball
 * @date 2021/8/26 14:25
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnsMessageListener {

    /**
     * 消费组
     */
    String groupId();

    /**
     * 主题
     */
    String topic();

    /**
     * 表达式类型，默认为TAG
     */
    ExpressionType expressionType() default ExpressionType.TAG;

    /**
     * 获取消息路由表达式
     */
    String expression() default "*";

    /**
     * 消息模型，支持集群和广播，默认为集群
     */
    MessageModel messageModel() default MessageModel.CLUSTERING;

    /**
     * 消费者线程数量，默认为20
     */
    int consumeThreadNums() default 20;

    /**
     * 消费超时时间（分钟），默认为15分钟
     */
    long consumeTimeout() default 15L;

    /**
     * BatchConsumer每次批量消费的最大消息数量, 默认值为1, 允许自定义范围为[1, 32], 实际消费数量可能小于该值
     * @return
     */
    int consumeMessageBatchMaxSize() default 1;


}
