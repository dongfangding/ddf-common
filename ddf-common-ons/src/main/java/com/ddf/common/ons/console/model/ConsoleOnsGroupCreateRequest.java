package com.ddf.common.ons.console.model;

import com.aliyun.ons20190214.models.OnsGroupCreateRequest;
import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.common.ons.console.constant.GroupTypeEnum;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>创建客户端GroupId</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/20 15:32
 */
@Data
@Accessors(chain = true)
public class ConsoleOnsGroupCreateRequest implements EnvRequest, UserRequest, Serializable {

    private static final long serialVersionUID = 4369719034030610370L;

    /**
     * 获取当前用户
     */
    private String currentUser;

    /**
     * 操作需要同步到哪些环境， 如果为空，则同步所有环境
     */
    private Set<String> envList;

    /**
     * Group_Id
     */
    @NotBlank(message = "GroupId不能为空")
    private String groupId;

    /**
     * 指定创建的Group ID适用的协议
     */
//    @NotNull(message = "GroupType不能为空")
//    private GroupTypeEnum groupType = GroupTypeEnum.TCP;

    /**
     * 备注
     */
    private String remark;


    /**
     * 转换为ONS SDK请求参数
     *
     * @param instanceId
     * @return
     */
    public OnsGroupCreateRequest toSdkRequest(String instanceId) {
        if (Objects.isNull(groupId) || (!groupId.startsWith("GID-") && !groupId.startsWith("GID_"))) {
            throw new IllegalArgumentException("Group ID 必须以 “GID_” 或者 “GID-” 开头。");
        }
        PreconditionUtil.checkArgument(StringUtils.isNotBlank(instanceId), new IllegalArgumentException("InstanceId不能为空"));
        return new OnsGroupCreateRequest()
                .setInstanceId(instanceId)
                .setGroupId(groupId)
                .setGroupType(GroupTypeEnum.TCP.getValue())
                .setRemark(remark);
    }
}
