package com.ddf.boot.common.stomp.model.dto;

import java.io.Serializable;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2026/09/08 18:29
 */
@Data
public class RedisBroadcastMsg implements Serializable {

    /**
     * 委托主机，即消息从哪台主机广播的
     */
    private String delegateHost;

    /**
     * 是否过滤委托主机，即广播后，委托主机也能收到这份消息，是否需要过滤这条消息避免重复处理
     */
    private boolean excludeDelegate;

    /**
     * 消息内容
     */
    private String msg;

}
