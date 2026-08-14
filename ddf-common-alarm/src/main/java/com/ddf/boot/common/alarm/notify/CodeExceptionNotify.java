package com.ddf.boot.common.alarm.notify;

import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.alarm.channel.AlarmChannel;
import com.ddf.boot.common.alarm.config.ExceptionAlarmProperties;
import com.ddf.boot.common.api.util.DateUtils;
import com.ddf.boot.common.core.event.GlobalExceptionEvent;
import com.ddf.boot.common.core.event.GlobalExceptionEventPayload;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * <p>description</p >
 *
 * @author rebot
 * @version 1.0
 * @since 2024/06/06 10:37
 */
@RequiredArgsConstructor
@Slf4j
public class CodeExceptionNotify implements ApplicationListener<GlobalExceptionEvent> {

    private final ThreadPoolTaskExecutor globalExceptionExecutor;
    private final ExceptionAlarmProperties exceptionAlarmProperties;
    private final List<AlarmChannel> alarmChannels;

    @Override
    public void onApplicationEvent(GlobalExceptionEvent event) {
        globalExceptionExecutor.execute(() -> {
            final GlobalExceptionEventPayload payload = event.getPayload();
            if (Objects.isNull(exceptionAlarmProperties) || !exceptionAlarmProperties.isEnabled()) {
                return;
            }
            final List<String> ignoreCodeOrMessageList = exceptionAlarmProperties.getIgnoreCodeOrMessageList();
            if (CollUtil.isNotEmpty(ignoreCodeOrMessageList)) {
                if (StringUtils.isNotBlank(payload.getErrorCode()) && ignoreCodeOrMessageList.contains(
                        payload.getErrorCode())) {
                    return;
                }
                if (StringUtils.isNotBlank(payload.getErrorMessage()) && ignoreCodeOrMessageList.contains(
                        payload.getErrorMessage())) {
                    return;
                }
            }
            String content = buildContent(payload);
            for (AlarmChannel channel : alarmChannels) {
                if (channel.isEnabled()) {
                    try {
                        channel.send("代码异常告警", content);
                    } catch (Exception e) {
                        log.error("发送告警失败, channel={}", channel.getChannelType(), e);
                    }
                }
            }
        });
    }

    private String buildContent(GlobalExceptionEventPayload payload) {
        StringBuilder sbl = new StringBuilder();
        sbl.append("# 服务信息: \n");
        sbl.append("## 应用名称与环境: \n").append(">").append(payload.getApplicationName()).append("[").append(
                payload.getProfile()).append("] \n");
        sbl.append("## 发生时间: \n").append(">").append(DateUtils.standardFormatMillis(payload.getTimestamps()))
                .append(" \n");
        sbl.append("## 主机: \n").append(">").append(payload.getHost()).append(" \n");
        sbl.append("# 接口信息: \n");
        sbl.append("## url: \n").append(">").append(payload.getUrl()).append(" \n");
        sbl.append("# 异常详情: \n").append(">").append(payload.getErrorMessage()).append(" \n");
        return sbl.toString();
    }
}
