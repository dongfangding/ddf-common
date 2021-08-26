package com.ddf.common.ons.console.model;

import com.aliyun.ons20190214.models.OnsTopicCreateRequest;
import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.common.ons.console.constant.TopicMessageType;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>带环境同步的创建TOPIC请求类</p >
 * <p>
 * <p>
 * https://next.api.aliyun.com/document/Ons/2019-02-14/OnsTopicCreate
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/17 15:47
 */
@Data
@Accessors(chain = true)
public class ConsoleOnsTopicCreateRequest implements EnvRequest, UserRequest, Serializable {

    private static final long serialVersionUID = -8649656233831333991L;

    /**
     * 获取当前用户
     */
    private String currentUser;

    /**
     * 操作需要同步到哪些环境， 如果为空，则同步所有环境
     */
    private Set<String> envList;

    /**
     * topic name
     */
    @NotBlank(message = "topic名称不能为空")
    public String topic;

    /**
     * topic消息类型
     *
     * @see TopicMessageType
     */
    @NotNull(message = "topic消息类型不能为空")
    public Integer messageType;

    /**
     * 备注
     */
    public String remark;

    /**
     * 转换为SDK内部提供的参数对象
     *
     * @return
     */
    public OnsTopicCreateRequest toSdkRequest(String instanceId) {
        PreconditionUtil.checkArgument(StringUtils.isNotBlank(instanceId), new IllegalArgumentException("InstanceId不能为空"));
        PreconditionUtil.checkArgument(Objects.nonNull(TopicMessageType.getByValue(messageType)), new IllegalArgumentException("消息类型值有误"));
        return new OnsTopicCreateRequest()
                .setTopic(topic)
                .setMessageType(messageType)
                .setRemark(remark)
                .setInstanceId(instanceId);
    }
}
