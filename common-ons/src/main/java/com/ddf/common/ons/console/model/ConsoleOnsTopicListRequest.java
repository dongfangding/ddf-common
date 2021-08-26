package com.ddf.common.ons.console.model;

import com.aliyun.ons20190214.models.OnsTopicListRequest;
import com.ddf.common.ons.console.config.EnvClientProperties;
import java.io.Serializable;
import lombok.Data;

/**
 * <p>查询账号下所有 Topic 的信息列表</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/25 13:12
 */
@Data
public class ConsoleOnsTopicListRequest implements UserRequest, Serializable {

    private static final long serialVersionUID = -8499670135779906009L;

    /**
     * 获取当前用户
     */
    private String currentUser;

    /**
     * 环境key
     * @see EnvClientProperties#getClients()
     */
    private String env;

    /**
     * Topic
     */
    private String topic;

    /**
     * 转换为SDK请求对象
     *
     * @param instanceId
     * @return
     */
    public OnsTopicListRequest toSdkRequest(String instanceId) {
        return new OnsTopicListRequest()
                .setTopic(topic)
                .setInstanceId(instanceId);
    }
}
