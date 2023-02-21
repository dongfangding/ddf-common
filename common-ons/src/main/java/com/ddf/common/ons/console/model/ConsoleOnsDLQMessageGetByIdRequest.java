package com.ddf.common.ons.console.model;

import com.aliyun.ons20190214.models.OnsDLQMessageGetByIdRequest;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <p>根据MessageId查询死信消息</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/24 13:44
 */
@Data
public class ConsoleOnsDLQMessageGetByIdRequest implements UserRequest, Serializable {

    private static final long serialVersionUID = 4145719272314986680L;

    /**
     * 消息id
     */
    @NotBlank(message = "消息id不能为空")
    private String messageId;

    /**
     * Group Id
     */
    @NotBlank(message = "GroupId不能为空")
    private String groupId;

    /**
     * 获取当前用户
     */
    private String currentUser;

    /**
     * 转换为SDK请求对象
     *
     * @param instanceId
     * @return
     */
    public OnsDLQMessageGetByIdRequest toSdkRequest(String instanceId) {
        return new OnsDLQMessageGetByIdRequest()
                .setMsgId(messageId)
                .setGroupId(groupId)
                .setInstanceId(instanceId);
    }
}
