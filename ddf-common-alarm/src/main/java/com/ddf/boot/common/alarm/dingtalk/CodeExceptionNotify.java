package com.ddf.boot.common.alarm.dingtalk;

import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.alarm.config.DingTalkProperties;
import com.ddf.boot.common.alarm.config.ExceptionAlarmProperties;
import com.ddf.boot.common.alarm.config.LarkProperties;
import com.ddf.boot.common.alarm.model.LarkContentRequest;
import com.ddf.boot.common.alarm.model.LarkTag;
import com.ddf.boot.common.alarm.util.DingTalkUtil;
import com.ddf.boot.common.alarm.util.LarkUtil;
import com.ddf.boot.common.api.util.DateUtils;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.core.event.GlobalExceptionEvent;
import com.ddf.boot.common.core.event.GlobalExceptionEventPayload;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @date 2024/06/06 10:37
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class CodeExceptionNotify implements ApplicationListener<GlobalExceptionEvent> {

    private final ThreadPoolTaskExecutor globalExceptionExecutor;
    private final DingTalkProperties dingTalkProperties;
    private final ExceptionAlarmProperties exceptionAlarmProperties;
    private final LarkProperties larkProperties;
    private final EnvironmentHelper environmentHelper;

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

            sendToLark(payload);
            sendToDingTalk(payload);
        });
    }


    /**
     * 发送到钉钉机器人
     *
     * @param payload
     */
    private void sendToDingTalk(GlobalExceptionEventPayload payload) {
        try {
            final DingTalkProperties.Properties propertiesBizResource = dingTalkProperties.getCodeException();
            if (!propertiesBizResource.isEnabled()) {
                return;
            }
            StringBuilder sbl = new StringBuilder();
            sbl.append("# 服务信息: \n");
            sbl
                    .append("## 应用名称与环境: \n")
                    .append(">")
                    .append(payload.getApplicationName())
                    .append("[")
                    .append(payload.getProfile())
                    .append("]")
                    .append(" \n");
            sbl
                    .append("## 发生时间: \n")
                    .append(">")
                    .append(DateUtils.standardFormatMillis(payload.getTimestamps()))
                    .append(" \n");
            sbl
                    .append("## 主机: \n")
                    .append(">")
                    .append(payload.getHost())
                    .append(" \n");
            sbl.append("# 设备信息: \n");
            sbl
                    .append("## 是否网关转发: \n")
                    .append(">")
                    .append(payload.getIsGatewayDispatch())
                    .append(" \n");
            sbl
                    .append("## imei: \n")
                    .append(">")
                    .append(payload.getImei())
                    .append(" \n");
            sbl
                    .append("## uid: \n")
                    .append(">")
                    .append(payload.getUid())
                    .append(" \n");
            sbl
                    .append("## os: \n")
                    .append(">")
                    .append(payload.getOs())
                    .append(" \n");
            sbl.append("# 接口信息: \n");
            sbl
                    .append("## url: \n")
                    .append(">")
                    .append(payload.getUrl())
                    .append(" \n");
            sbl
                    .append("## 查询参数: \n")
                    .append(">")
                    .append(JsonUtil.toJson(payload.getParameterMap()))
                    .append(" \n");
            sbl
                    .append("## 请求体: \n")
                    .append(">")
                    .append(payload.getBody())
                    .append(" \n");
            sbl
                    .append("# 异常详情: \n")
                    .append(">")
                    .append(payload.getErrorMessage())
                    .append(" \n");
            DingTalkUtil.sendMarkdownMsgToAllWithLimit(propertiesBizResource.getSecret(),
                    propertiesBizResource.getAccessToken(), "代码异常告警", sbl.toString()
            );
        } catch (Exception e) {
            log.error("发送钉钉异常告警失败", e);
        }
    }


    /**
     * 发送到Lark机器人
     *
     * @param payload
     */
    private void sendToLark(GlobalExceptionEventPayload payload) {
        try {
            final String applicationName = environmentHelper.getApplicationName();
            final LarkProperties.Properties propertiesBizResource = larkProperties.getCodeProperties(applicationName);
            if (!propertiesBizResource.isEnabled()) {
                return;
            }
            LarkContentRequest request = new LarkContentRequest();
            final List<List<LarkTag>> lists = List.of(List.of(LarkTag.buildText(
                            "应用名称与环境: " + payload.getApplicationName() + "[" + payload.getProfile() + "]")),
                    List.of(LarkTag.buildText("发生时间: " + DateUtils.standardFormatMillis(payload.getTimestamps()))),
                    List.of(LarkTag.buildText("主机: " + payload.getHost())), List.of(LarkTag.buildText(
                            "调用方式: " + (payload.getIsGatewayDispatch() ?
                                    "网关转发" + " | uid: " + payload.getUid() + " | os: " + payload.getOs()
                                            + " | imei: " + payload.getImei() + " | versionCode: "
                                            + payload.getVersionCode() : "内部调用"))),
                    List.of(LarkTag.buildText("url: " + payload.getUrl())),
                    List.of(LarkTag.buildText("查询参数: " + JsonUtil.toJson(payload.getParameterMap()))),
                    List.of(LarkTag.buildText("请求体: " + payload.getBody())),
                    List.of(LarkTag.buildText("请求头: " + payload.getClientHeaderMap())),
                    List.of(LarkTag.buildText("异常详情: " + payload.getErrorMessage())), List.of(LarkTag.buildAtAll())
            );
            request.setContent(lists);
            LarkUtil.sendPostMsgType(propertiesBizResource.getWebhookUrl(), propertiesBizResource.getSecret(),
                    "Java代码异常告警", request
            );
        } catch (Exception e) {
            log.error("发送lark异常告警失败", e);
        }

    }
}
