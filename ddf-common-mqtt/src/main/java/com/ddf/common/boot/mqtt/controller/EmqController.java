package com.ddf.common.boot.mqtt.controller;

import com.ddf.common.boot.mqtt.config.properties.EmqConnectionProperties;
import com.ddf.common.boot.mqtt.enume.MQTTProtocolEnum;
import com.ddf.common.boot.mqtt.extra.EmqClientAuthenticate;
import com.ddf.common.boot.mqtt.model.request.emq.ConnectionInfoRequest;
import com.ddf.common.boot.mqtt.model.request.emq.EmqAuthenticateRequest;
import com.ddf.common.boot.mqtt.model.response.ConnectionInfoResponse;
import com.ddf.common.boot.mqtt.model.response.emq.EmqClientAuthenticateResponse;
import com.ddf.common.boot.mqtt.util.EmqHttpResponseUtil;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>提供emq相关的开放接口功能</p >
 * 该接口必须部署在开放服务中
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/22 14:46
 */
@RestController
@RequestMapping("emq")
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class EmqController {

    private final EmqConnectionProperties emqConnectionProperties;

    @Autowired(required = false)
    private EmqClientAuthenticate emqClientAuthenticate;

    /**
     * 获取emq连接信息
     *
     * @param request
     * @return
     */
    @GetMapping("getConnectionInfo")
    public ConnectionInfoResponse getConnectionInfo(@RequestBody ConnectionInfoRequest request) {
        final String protocol = request.getProtocol();
        final MQTTProtocolEnum protocolEnum = MQTTProtocolEnum.resolve(protocol);
        if (Objects.isNull(protocolEnum)) {
            return ConnectionInfoResponse.of(protocol, null, "不支持的协议地址");
        }
        final EmqConnectionProperties.ConnectionConfig connectionConfig = emqConnectionProperties.getConnectionUrl(protocol);
        String url = Objects.isNull(connectionConfig) ? "" : connectionConfig.getUrl();
        return ConnectionInfoResponse.of(protocol, url, "成功");
    }

    /**
     * 连接认证
     *
     * @param request
     */
    @PostMapping("authenticate")
    public void authenticate(@RequestBody EmqAuthenticateRequest request, HttpServletResponse response) {
        final EmqConnectionProperties.ClientConfig client = emqConnectionProperties.getClient();
        final String username = client.getUsername();
        final String password = client.getPassword();
        final String clientId = client.getClientIdPrefix();

        // 服务端用户
        if (request.getClientId().startsWith(clientId)) {
            // 匹配用户名和密码
            if (Objects.equals(username, request.getUsername()) && Objects.equals(password, request.getPassword())) {
                EmqHttpResponseUtil.success(response, "服务端连接认证通过");
            } else {
                EmqHttpResponseUtil.error(response, "用户名和密码不匹配，服务端连接认证失败");
            }
        } else {
            // 客户端用户， 让使用该模块的功能完成自己的用户认证
            if (emqClientAuthenticate == null) {
                EmqHttpResponseUtil.success(response, "客户端不校验权限，认证通过");
                return;
            }
            final EmqClientAuthenticateResponse authenticate = emqClientAuthenticate.authenticate(request);
            if (authenticate.isResult()) {
                EmqHttpResponseUtil.success(response, "客户端连接认证通过");
            } else {
                EmqHttpResponseUtil.error(response, "客户端连接认证不通过，原因: " + authenticate.getMsg());
            }
        }
    }


    /**
     * ACL 超级用户认证
     * 首先查询客户端是否为超级用户，客户端为超级用户时将跳过 ACL 查询。
     *
     * @param request
     */
    @PostMapping("superuser")
    public void superuser(@RequestBody EmqAuthenticateRequest request, HttpServletResponse response) {
        final EmqConnectionProperties.ClientConfig client = emqConnectionProperties.getClient();
        final String reqClientId = request.getClientId();
        if (reqClientId.startsWith(client.getClientIdPrefix())) {
            EmqHttpResponseUtil.success(response, "超级用户ACL认证通过");
            return;
        }
        EmqHttpResponseUtil.error(response, "超级用户ACL未认证通过");
    }
}
