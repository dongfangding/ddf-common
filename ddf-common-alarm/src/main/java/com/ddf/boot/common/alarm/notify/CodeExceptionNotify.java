package com.ddf.boot.common.alarm.notify;

import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.alarm.channel.AlarmChannel;
import com.ddf.boot.common.alarm.channel.AlarmFrequencyControl;
import com.ddf.boot.common.alarm.config.ExceptionAlarmProperties;
import com.ddf.boot.common.alarm.model.AlarmMessage;
import com.ddf.boot.common.api.util.DateUtils;
import com.ddf.boot.common.core.event.GlobalExceptionEvent;
import com.ddf.boot.common.core.event.GlobalExceptionEventPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
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
    private final ObjectProvider<AlarmFrequencyControl> alarmFrequencyControlProvider;

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
            AlarmFrequencyControl frequencyControl = alarmFrequencyControlProvider.getIfAvailable();
            if (Objects.nonNull(frequencyControl) && !frequencyControl.tryAcquire(payload.getErrorCode())) {
                return;
            }
            AlarmMessage message = buildMessage(payload);
            for (AlarmChannel channel : alarmChannels) {
                if (channel.isEnabled()) {
                    try {
                        channel.send(message);
                    } catch (Exception e) {
                        log.error("发送告警失败, channel={}", channel.getChannelType(), e);
                    }
                }
            }
        });
    }

    private AlarmMessage buildMessage(GlobalExceptionEventPayload payload) {
        List<String> lines = new ArrayList<>();
        lines.add("【服务信息】");
        lines.add("应用名称与环境: " + payload.getApplicationName() + "[" + payload.getProfile() + "]");
        lines.add("发生时间: " + DateUtils.standardFormatMillis(payload.getTimestamps()));
        lines.add("主机: " + payload.getHost());
        lines.add("【设备信息】");
        lines.add("是否网关转发: " + payload.getIsGatewayDispatch());
        lines.add("imei: " + payload.getImei());
        lines.add("uid: " + payload.getUid());
        lines.add("os: " + payload.getOs());
        lines.add("【接口信息】");
        lines.add("url: " + payload.getUrl());
        lines.add("【异常详情】");
        lines.add(String.valueOf(payload.getErrorMessage()));
        return new AlarmMessage("代码异常告警", lines);
    }
}
