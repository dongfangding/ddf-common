package com.ddf.boot.common.alarm.dingtalk;

import com.ddf.boot.common.alarm.config.DingTalkProperties;
import com.ddf.boot.common.alarm.util.DingTalkUtil;
import com.ddf.boot.common.api.util.DateUtils;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.core.event.GlobalExceptionEvent;
import com.ddf.boot.common.core.event.GlobalExceptionEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2024/06/06 10:37
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class CodeExceptionNotify implements ApplicationListener<GlobalExceptionEvent> {

    private final ThreadPoolTaskExecutor globalExceptionExecutor;
    private final DingTalkProperties dingTalkProperties;

    @Override
    public void onApplicationEvent(GlobalExceptionEvent event) {
        globalExceptionExecutor.execute(() -> {
            final GlobalExceptionEventPayload payload = event.getPayload();
            final DingTalkProperties.Properties propertiesBizResource = dingTalkProperties.getCodeException();
            StringBuilder sbl = new StringBuilder();
            sbl.append("# 服务信息: \n");
            sbl.append("## 应用名称与环境: \n").append(">").append(payload.getApplicationName()).append("[").append(payload.getProfile()).append("]").append(" \n");
            sbl.append("## 发生时间: \n").append(">").append(DateUtils.standardFormatMillis(payload.getTimestamps())).append(" \n");
            sbl.append("## 主机: \n").append(">").append(payload.getHost()).append(" \n");
            sbl.append("# 接口信息: \n");
            sbl.append("## url: \n").append(">").append(payload.getUrl()).append(" \n");
            sbl.append("## 查询参数: \n").append(">").append(JsonUtil.toJson(payload.getParameterMap())).append(" \n");
            sbl.append("## 请求体: \n").append(">").append(payload.getBody()).append(" \n");
            sbl.append("# 异常详情: \n").append(">").append(payload.getErrorMessage()).append(" \n");
            DingTalkUtil.sendMarkdownMsgToAll(propertiesBizResource.getSecret(), propertiesBizResource.getAccessToken(),
                    "代码异常告警", sbl.toString());
        });
    }
}
