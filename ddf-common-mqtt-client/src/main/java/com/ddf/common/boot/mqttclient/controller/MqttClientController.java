package com.ddf.common.boot.mqttclient.controller;

import com.ddf.boot.common.api.model.common.response.ResponseData;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.core.util.BeanCopierUtils;
import com.ddf.common.boot.mqtt.client.MqttPublishClient;
import com.ddf.common.boot.mqtt.model.request.InnerMqttMessageRequest;
import com.ddf.common.boot.mqttclient.model.request.MqttMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>提供emq相关的开放接口功能</p >
 * 该接口必须部署在开放服务中， 这里只是实例代码，实际项目中使用要把代码放出去到开放服务中，而不是直接使用该模块中的控制器代码。
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/22 14:46
 */
@RestController
@RequestMapping("mqtt/client")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class MqttClientController {
    private final MqttPublishClient mqttPublishClient;

    /**
     * 发布消息
     *
     * @param request
     * @return
     */
    @PostMapping("publish")
    public ResponseData<Boolean> publish(@RequestBody MqttMessageRequest request) {
        final InnerMqttMessageRequest innerMqttMessageRequest = BeanCopierUtils.copy(request, InnerMqttMessageRequest.class);
        innerMqttMessageRequest.setTopic(request.getTopic().getFullTopic());
        innerMqttMessageRequest.setBody(JsonUtil.toJson(request.getBody()));
        mqttPublishClient.publish(innerMqttMessageRequest);
        return ResponseData.success(true);
    }
}
