package com.ddf.common.ids.service.model.common;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>leader 节点数据</p >
 *
 * @author shuaishuai.xiao
 * @version 1.0: LeaderNodeData.java
 * @date 2020/1/11 17:43
 */
@Data
@Accessors(chain = true)
public class LeaderNodeData implements Serializable {
    /**
     * 分组
     */
    private String group;
    /**
     * 接口
     */
    private String interfaceCls;
    /**
     * 注册地址
     */
    private String registryUrl;
    /**
     * 放入时间
     */
    private long timestamp;
    /**
     * 版本号
     */
    private String version;
}
