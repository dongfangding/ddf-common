package com.ddf.boot.zookeeper.monitor.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/10/09 13:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonitorNode {

    public static final String HOST_MODE_AUTO = "AUTO";

    /**
     * 要监控的端点主机地址
     *
     * 如果为AUTO, 则为使用当前服务的ip+端口号
     */
    private String monitorHost = HOST_MODE_AUTO;

    /**
     * 要监控的端点路径, 需要自己写标准路径分隔符/
     */
    private String monitorPath;

    // todo 抽出CreateMode这个属性

    /**
     * 节点被创建后，是否使用默认的基于时间戳的数据上报， 如果不使用，也可以自己去实现
     */
    private boolean useDefaultTimeStampUpload;
}
