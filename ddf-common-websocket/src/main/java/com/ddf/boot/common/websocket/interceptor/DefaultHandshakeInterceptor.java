package com.ddf.boot.common.websocket.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.NetUtil;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.websocket.constant.WebsocketConst;
import com.ddf.boot.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.boot.common.websocket.model.AuthPrincipal;
import com.ddf.boot.common.websocket.model.HandshakeParam;
import com.ddf.boot.common.websocket.model.WebSocketSessionWrapper;
import com.ddf.boot.common.websocket.properties.WebSocketProperties;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * 握手前的拦截器，认证客户端是否允许握手
 * WebSocketHandlerDecorator
 * WebSocketHttpRequestHandler
 *
 * @author dongfang.ding
 * @date 2019/8/20 11:46
 */
@Slf4j
public class DefaultHandshakeInterceptor implements HandshakeInterceptor {

    private final WebSocketProperties webSocketProperties;

    private final List<HandshakeAuth> handshakeAuthList;

    private static final Map<String, EncryptProcessor> ENCRYPT_PROCESSORS = SpringContextHolder.getBeansOfType(
            EncryptProcessor.class);

    public DefaultHandshakeInterceptor(WebSocketProperties webSocketProperties, List<HandshakeAuth> handshakeAuthList) {
        this.webSocketProperties = webSocketProperties;
        this.handshakeAuthList = handshakeAuthList;
    }

    private static final String REAL_IP_HEADER = "X-Real-IP";

    /**
     * Invoked before the handshake is processed.
     *
     * @param request    the current request
     * @param response   the current response
     * @param wsHandler  the target WebSocket handler
     * @param attributes attributes from the HTTP handshake to associate with the WebSocket
     *                   session; the provided attributes are copied, the original map is not used.
     * @return whether to proceed with the handshake ({@code true}) or abort ({@code false})
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {
        ServletServerHttpRequest req = (ServletServerHttpRequest) request;
        HttpServletRequest servletRequest = req.getServletRequest();
        Principal principal = req.getPrincipal();
        if (principal != null) {
            return true;
        }

        if (CollUtil.isEmpty(handshakeAuthList)) {
            response.getBody().write("没有实现的握手处理器".getBytes(StandardCharsets.UTF_8));
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.flush();
            return false;
        }

        AuthPrincipal authPrincipal = null;

        // fixme 如何在head中传递？？ 先Url编解码是为了防止客户端并不希望对token参数本身进行加密， 而json默认是无法传输过来的，所以要url编码，写在最
        // 外层可以让代码更简单一些
        String token = URLDecoder.decode(servletRequest.getParameter(WebsocketConst.TOKEN_PARAMETER), "utf-8");
        HandshakeParam handshakeParam = null;
        if (StringUtils.isNotBlank(token)) {
            try {
                if (webSocketProperties.isHandshakeTokenSecret()) {
                    EncryptProcessor encryptProcessor = ENCRYPT_PROCESSORS.get(webSocketProperties.getSecretBeanName());
                    if (encryptProcessor == null) {
                        throw new NoSuchBeanDefinitionException(webSocketProperties.getSecretBeanName());
                    }
                    token = ENCRYPT_PROCESSORS.get(webSocketProperties.getSecretBeanName()).decryptHandshakeToken(
                            token);
                }
                handshakeParam = JsonUtil.toBean(token, HandshakeParam.class);
                if (!validArgument(handshakeParam, response)) {
                    return false;
                }
            } catch (Exception e) {
                log.error("认证参数反序列化失败！参数为: [{}]", token, e);
                response.getBody().write("认证参数反序列化失败".getBytes(StandardCharsets.UTF_8));
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
        }

        // 调用身份认证接口
        for (HandshakeAuth handshakeAuth : handshakeAuthList) {
            authPrincipal = handshakeAuth.validPrincipal(request, response, wsHandler, attributes, handshakeParam);
            if (authPrincipal != null) {
                break;
            }
        }

        if (authPrincipal == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getBody().write("所有认证器都无法验证参数身份".getBytes(StandardCharsets.UTF_8));
            return false;
        }

        // 与Nginx约定，Nginx需要将真实的ip放到请求头中
        if (StringUtils.isNotBlank(request.getHeaders().getFirst(REAL_IP_HEADER))) {
            attributes.put(WebsocketConst.CLIENT_REAL_IP, request.getHeaders().getFirst(REAL_IP_HEADER));
        } else {
            attributes.put(WebsocketConst.CLIENT_REAL_IP, req.getRemoteAddress().getAddress().getHostAddress());
        }
        attributes.put(WebsocketConst.SERVER_IP, NetUtil.getLocalhostStr());
        attributes.put(WebsocketConst.PRINCIPAL_KEY, authPrincipal);

        // 同一个账号多个地方登陆
        WebSocketSessionWrapper oldSession = WebsocketSessionStorage.get(authPrincipal);
        if (oldSession != null && oldSession.getStatus() == 1) {
            try {
                if (authPrincipal.getTimeStamp().equals(oldSession.getAuthPrincipal().getTimeStamp())) {
                    // 如果第一次登录生成的登录参数设备还在线，不允许相同的参数token直接拿走登录，如果不相同，则允许挤掉
                    log.error("同一设备登录时间戳不允许多次使用！");
                    return false;
                }
                String newClientAddress = req.getRemoteAddress().getAddress().getHostAddress();
                oldSession.getWebSocketSession().sendMessage(
                        new TextMessage("账号在别处[" + newClientAddress + "]登陆，即将断开连接。。。"));
                oldSession.getWebSocketSession().close();
            } catch (Exception exception) {
                log.error(ExceptionUtils.getStackTrace(exception));
            }
        }
        return true;
    }

    /**
     * Invoked after the handshake is done. The response status and headers indicate
     * the results of the handshake, i.e. whether it was successful or not.
     *
     * @param request   the current request
     * @param response  the current response
     * @param wsHandler the target WebSocket handler
     * @param exception an exception raised during the handshake, or {@code null} if none
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Exception exception) {

    }

    /**
     * 参数校验
     *
     * @param handshakeParam
     * @param response
     * @return
     * @throws IOException
     */
    private boolean validArgument(HandshakeParam handshakeParam, ServerHttpResponse response) throws IOException {
        if (StringUtils.isAnyBlank(handshakeParam.getAccessKeyId(), handshakeParam.getAuthCode())) {
            log.error("关键字信息和授权码都不能为空！, {}, {}", handshakeParam.getAccessKeyId(), handshakeParam.getAuthCode());
            response.getBody().write("关键字信息和授权码都不能为空".getBytes(StandardCharsets.UTF_8));
            return false;
        }

        if (handshakeParam.getLoginType() == null) {
            log.error("登录类型不能为空！");
            response.getBody().write("登录类型不能为空".getBytes(StandardCharsets.UTF_8));
            return false;
        }

        if (!webSocketProperties.isIgnoreAuthTimestamp()) {
            long now = System.currentTimeMillis();
            if (Math.abs(now - handshakeParam.getCurrentTimeStamp()) > webSocketProperties.getValidAuthTimeStamp()) {
                log.error("认证参数已过期{}==>{}", now, handshakeParam.getCurrentTimeStamp());
                response.getBody().write(String.format("认证参数已过期%s==>%s", now, handshakeParam.getCurrentTimeStamp())
                        .getBytes(StandardCharsets.UTF_8));
                return false;
            }
        }
        return true;
    }
}
