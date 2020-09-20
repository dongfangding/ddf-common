package com.ddf.boot.netty.broker.server.properties;

import io.netty.channel.ChannelOption;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 代理服务器配置类$
 *
 * @author dongfang.ding
 * @date 2020/9/20 0020 21:31
 */
@Component
@Data
@ConfigurationProperties(prefix = "netty-broker")
public class BrokerProperties {

    /**
     * 服务端端口号
     */
    private int port = 9999;

    /**
     * 是否使用ssl连接
     */
    private boolean ssl;

    /**
     * @see ChannelOption#SO_SNDBUF
     */
    private Integer soSndBuf = 1048576;

    /**
     *
     * @see ChannelOption#SO_RCVBUF
     */
    private Integer soRecBuf = 1048576;

    /**
     * 心跳检查允许客户端的空闲时间，单位秒
     */
    private Integer allowIdleSeconds = 60;

}
