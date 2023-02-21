package com.ddf.common.ons.mongodb;

import com.ddf.common.ons.console.model.UserRequest;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * ONS消息日志查询参数
 *
 * @author snowball
 * @date 2021/8/26 15:30
 **/
@Data
public class OnsProduceMessageLogQueryVO extends OnsMessageLogQueryVO implements UserRequest, Serializable {

    private static final long serialVersionUID = 6734114987284936881L;

    /**
     * 获取当前用户
     */
    private String currentUser;

    /**
     * 分页页码
     */
    @NotNull(message = "分页页码不能为空")
    private Integer pageNum;

    /**
     * 分页每页大小
     */
    @NotNull(message = "分页每页大小不能为空")
    private Integer pageSize;

    /**
     * 主题
     */
    private String topic;

    /**
     * 路由表达式
     */
    private String expression;

    /**
     * 业务Id，每次发送必须唯一
     */
    private String bizId;

    /**
     * 顺序消息必传
     * 分区顺序消息中区分不同分区的关键字段，Sharding Key 与普通消息的 key 是完全不同的概念。
     * 全局顺序消息，该字段可以设置为任意非空字符串。
     */
    private String shadingKey;

    /**
     * 消息发送/消费开始时间
     */
    private Long messageStartTime;

    /**
     * 消息发送/消费结束时间
     */
    private Long messageEndTime;

}
