package com.ddf.common.ids.service.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>id生成器配置类</p >
 *
 * @author Mitchell
 * @version 1.0
 * @date 2021/02/23 14:46
 */
@Data
@ConfigurationProperties(prefix = "customs.ids")
public class IdsProperties {

    public static final String IDS_PROPERTIES_PREFIX = "customs.ids";

    /**
     * 名称， 雪花id时会作为存储数据的前缀节点
     */
    private String name = "customs.ids";

    /**
     * 可以设定一个起始时间戳，可以达到混淆实际输出雪花id中包含的时间
     * 2021-01-01 00:00:00
     */
    private Long beginTimestamp = 1609430400000L;

    /**
     * zookeeper连接地址， 支持集群
     */
    private String zkAddress;

    /**
     * 这个port并不参与实际的连接，只是监视客户端连接时使用ip和端口标识一个机器， 如果是一台机器伪集群时可以使用这个port区别
     */
    private Integer port = 2181;

    /**
     * 号段模式是否开启
     */
    private boolean segmentEnable;

}
