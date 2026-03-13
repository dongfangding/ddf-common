package com.ddf.boot.common.alarm.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * <p>description</p >
 *
 * @author rebot
 * @version 1.0
 * @since 2024/07/12 14:38
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "customs.alarm.exception")
public class ExceptionAlarmProperties {

    /**
     * 是否开启异常告警
     */
    private boolean enabled = true;

    /**
     * 要忽略错误告警的异常状态码或者消息，因为某些接口，其实是需要告警的，只是其中一个错误本来也是需要告警的，例如支付， 但是因为账号被封了，
     * 等待解封期间，可以关闭告警
     */
    private List<String> ignoreCodeOrMessageList;

    /**
     * 要忽略错误告警的接口
     */
    private List<String> ignoreUrlList;
}
