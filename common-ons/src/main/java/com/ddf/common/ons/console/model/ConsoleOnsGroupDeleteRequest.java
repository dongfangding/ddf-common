package com.ddf.common.ons.console.model;

import com.aliyun.ons20190214.models.OnsGroupDeleteRequest;
import java.io.Serializable;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/21 15:49
 */
@Data
@Accessors(chain = true)
public class ConsoleOnsGroupDeleteRequest implements EnvRequest, UserRequest, Serializable {

    private static final long serialVersionUID = -1025899637278278369L;

    /**
     * 操作需要同步到哪些环境， 如果为空，则同步所有环境
     */
    private Set<String> envList;

    /**
     * topic name
     */
    @NotBlank(message = "topic名称不能为空")
    private String groupId;

    /**
     * 获取当前用户
     */
    private String currentUser;

    /**
     * 转换为SDK内部提供的参数对象
     *
     * @return
     */
    public OnsGroupDeleteRequest toSdkRequest(String instanceId) {
        return new OnsGroupDeleteRequest()
                .setGroupId(groupId)
                .setInstanceId(instanceId);
    }
}
