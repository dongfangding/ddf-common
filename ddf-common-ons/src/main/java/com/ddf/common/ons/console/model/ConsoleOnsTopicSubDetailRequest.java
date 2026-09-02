package com.ddf.common.ons.console.model;

import com.aliyun.ons20190214.models.OnsTopicSubDetailRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <p>查看 Topic 的在线订阅组</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2021/05/25 13:51
 */
@Data
public class ConsoleOnsTopicSubDetailRequest implements UserRequest {

    /**
     * 获取当前用户
     */
    private String currentUser;

    /**
     * 环境key，对应 {@code EnvClientProperties#getClients()} 中的 key
     */
    @NotBlank(message = "环境key不能为空")
    private String env;

    /**
     * 查询的Topic
     */
    @NotBlank(message = "Topic不能为空")
    private String topicId;


    /**
     * 转换为SDK请求参数
     *
     * @param instanceId 实例ID
     */
    public OnsTopicSubDetailRequest toSdkRequest(String instanceId) {
        return new OnsTopicSubDetailRequest().setTopic(getTopicId()).setInstanceId(instanceId);
    }


}
