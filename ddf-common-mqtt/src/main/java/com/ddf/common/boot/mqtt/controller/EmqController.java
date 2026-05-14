package com.ddf.common.boot.mqtt.controller;

import com.ddf.boot.common.api.model.common.response.ResponseData;
import com.ddf.common.boot.mqtt.client.MqttPublishClient;
import com.ddf.common.boot.mqtt.config.properties.EmqConnectionProperties;
import com.ddf.common.boot.mqtt.enume.MQTTProtocolEnum;
import com.ddf.common.boot.mqtt.extra.EmqClientAuthenticate;
import com.ddf.common.boot.mqtt.model.request.InnerMqttMessageRequest;
import com.ddf.common.boot.mqtt.model.request.emq.ConnectionInfoRequest;
import com.ddf.common.boot.mqtt.model.request.emq.EmqAclRequest;
import com.ddf.common.boot.mqtt.model.request.emq.EmqAuthenticateRequest;
import com.ddf.common.boot.mqtt.model.response.ConnectionInfoResponse;
import com.ddf.common.boot.mqtt.model.response.MqttMessageResponse;
import com.ddf.common.boot.mqtt.model.response.emq.EmqClientAuthenticateResponse;
import com.ddf.common.boot.mqtt.support.GlobalStorage;
import com.ddf.common.boot.mqtt.util.EmqHttpResponseUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
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
 * @since 2022/03/22 14:46
 */
@RestController
@RequestMapping("/mqtt/proxy")
@RequiredArgsConstructor
@Slf4j
public class EmqController {

    private final EmqConnectionProperties emqConnectionProperties;
    private final MqttAsyncClient mqttAsyncClient;
    private final MqttPublishClient mqttPublishClient;
    private final ObjectProvider<EmqClientAuthenticate> emqClientAuthenticateProvider;

    /**
     * 演示发送消息，非正式使用
     *
     * @param message 消息内容
     */
    @GetMapping("send")
    public ResponseData<String> send(String message) throws MqttException {
        final MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(message.getBytes(StandardCharsets.UTF_8));
        mqttAsyncClient.publish("test", mqttMessage);
        return ResponseData.success("true");
    }


    /**
     * 原始发布消息，忽略处理一些规则，使用String接受参数，否则无法反序列化，定制化的接口在这个上层包装再处理
     *
     * @param request 请求对象
     */
    @PostMapping("publish")
    public ResponseData<MqttMessageResponse> publish(@RequestBody InnerMqttMessageRequest request) {
        return mqttPublishClient.publish(request);
    }

    /**
     * 获取emq连接信息
     *
     * @param request 请求对象
     */
    @GetMapping("getConnectionInfo")
    public ResponseData<ConnectionInfoResponse> getConnectionInfo(ConnectionInfoRequest request) {
        final String protocol = request.getProtocol();
        final MQTTProtocolEnum protocolEnum = MQTTProtocolEnum.resolve(protocol);
        if (Objects.isNull(protocolEnum)) {
            return ResponseData.failure("protocol not support", "protocol not support");
        }
        final EmqConnectionProperties.ConnectionConfig connectionConfig = emqConnectionProperties.getConnectionUrl(
                protocol);
        String url = Objects.isNull(connectionConfig) ? "" : connectionConfig.getUrl();
        return ResponseData.success(ConnectionInfoResponse.of(protocol, url));
    }

    /**
     * 连接认证
     *
     * @param request 请求对象
     * @param response 响应对象
     */
    @PostMapping("authenticate")
    public void authenticate(@RequestBody EmqAuthenticateRequest request, HttpServletResponse response) {
        try {
            final EmqConnectionProperties.ClientConfig client = emqConnectionProperties.getClient();
            final String username = client.getUsername();
            final String password = client.getPassword();
            final String clientId = client.getClientIdPrefix();

            // 服务端用户
            if (request.getClientId().startsWith(clientId)) {
                if (StringUtils.isAllBlank(username, password)) {
                    EmqHttpResponseUtil.success(response, "服务端未配置用户名和密码无需校验，服务端连接认证通过");
                    return;
                }
                // 匹配用户名和密码
                if (Objects.equals(username, request.getUsername()) && Objects.equals(password,
                        request.getPassword())) {
                    EmqHttpResponseUtil.success(response, "服务端连接认证通过");
                } else {
                    EmqHttpResponseUtil.error(response, "用户名和密码不匹配，服务端连接认证失败");
                }
            } else {
                // 客户端用户， 让使用该模块的功能完成自己的用户认证， 这块的代码应该写在应用层，而不是这个模块内部，因为如果是模块内部那就是自己依赖自己，
                // 本身服务没起来的前提所有客户端都无法连接， 所以这个代码应该是一个独立的认证中心，比如写在用户模块，然后接口暴露在网关层，然后将网关层接口
                // 配置到emq的认证http地址中，这里只是提供写法，小项目单体项目可以直接集成，分布式不合适。
                final EmqClientAuthenticate emqClientAuthenticate = emqClientAuthenticateProvider.getIfAvailable();
                if (emqClientAuthenticate == null) {
                    EmqHttpResponseUtil.error(response, "未定义客户端认证规则，不允许连接");
                    return;
                }
                final EmqClientAuthenticateResponse authenticate = emqClientAuthenticate.authenticate(request);
                if (authenticate.isResult()) {
                    EmqHttpResponseUtil.success(response, "客户端连接认证通过");
                } else {
                    EmqHttpResponseUtil.error(response, "客户端连接认证不通过，原因: " + authenticate.getMsg());
                }
            }
        } catch (Exception e) {
            log.error("mqtt连接认证失败", e);
            EmqHttpResponseUtil.error(response, "认证失败" + e.getMessage());
        }
    }


    /**
     * ACL 超级用户认证
     * 首先查询客户端是否为超级用户，客户端为超级用户时将跳过 ACL 查询。
     *
     * @param request 请求对象
     * @param response 响应对象
     */
    @PostMapping("acl/superuser")
    public void superuser(@RequestBody EmqAuthenticateRequest request, HttpServletResponse response) {
        final EmqConnectionProperties.ClientConfig client = emqConnectionProperties.getClient();
        final String reqClientId = request.getClientId();
        if (reqClientId.startsWith(client.getClientIdPrefix())) {
            EmqHttpResponseUtil.success(response, "超级用户ACL认证通过");
            return;
        }
        EmqHttpResponseUtil.error(response, "超级用户ACL未认证通过");
    }

    /**
     * 普通客户端ACL认证
     *
     * @param request 请求对象
     * @param response 响应对象
     */
    @PostMapping("acl")
    public void acl(@RequestBody EmqAclRequest request, HttpServletResponse response) {
        if (request.getTopic().contains(GlobalStorage.WILDCARD_CHARACTER)) {
            EmqHttpResponseUtil.error(response, "普通用户ACL未认证通过");
        }
        // 普通用户（一般为客户端）不允许直接使用调用底层的连接进行发布，必须请求服务端接口，服务端接口使用超级用户进行发布数据
        if (Objects.equals("2", request.getAction())) {
            EmqHttpResponseUtil.error(response, "普通用户ACL未认证通过");
        }
    }
}
