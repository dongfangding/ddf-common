package com.ddf.common.ons.console.model;

import com.aliyun.ons20190214.models.OnsGroupListRequest;
import com.ddf.common.ons.console.config.EnvClientProperties;
import com.ddf.common.ons.console.constant.GroupTypeEnum;
import java.io.Serializable;
import lombok.Data;

/**
 * <p>获取Group_Id资源列表</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/25 13:38
 */
@Data
public class ConsoleOnsGroupListRequest implements UserRequest, Serializable {

    private static final long serialVersionUID = -5424208858377211727L;

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
     * Group_Id
     */
    private String groupId;


    /**
     * 转换为SDK内部请求类
     *
     * @param instanceId
     * @return
     */
    public OnsGroupListRequest toSdkRequest(String instanceId) {
        return new OnsGroupListRequest()
                .setGroupId(getGroupId())
                .setGroupType(GroupTypeEnum.TCP.getValue())
                .setInstanceId(instanceId);
    }


}
