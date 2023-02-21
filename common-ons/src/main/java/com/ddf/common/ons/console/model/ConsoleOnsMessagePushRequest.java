// This file is auto-generated, don't edit it. Thanks.
package com.ddf.common.ons.console.model;

import com.aliyun.ons20190214.models.OnsMessagePushRequest;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * 向指定的消费者推送消息， 与原生请求不同的是这里不需要调用方指定ClientId和InstanceId属性， 会在方法里自动包装获取
 *
 */
@Data
@Accessors(chain = true)
public class ConsoleOnsMessagePushRequest implements UserRequest, Serializable {

    private static final long serialVersionUID = 3454902718353822995L;

    /**
     * 获取当前用户
     */
    private String currentUser;

    /**
     * 消费组
     */
    @NotBlank(message = "消费组id不能为空")
    public String groupId;

    /**
     * 消息id
     */
    @NotBlank(message = "消息id不能为空")
    public String msgId;

    /**
     * topic名称
     */
    @NotBlank(message = "topic名称不能为空")
    public String topic;

    /**
     * 转换为原生SDK请求参数对象
     *
     * @param clientId
     * @param instanceId
     * @return
     */
    public OnsMessagePushRequest toSdkRequest(String clientId, String instanceId) {
        return new OnsMessagePushRequest()
                .setGroupId(groupId)
                .setMsgId(msgId)
                .setTopic(topic)
                .setInstanceId(instanceId)
                .setClientId(clientId);
    }
}
