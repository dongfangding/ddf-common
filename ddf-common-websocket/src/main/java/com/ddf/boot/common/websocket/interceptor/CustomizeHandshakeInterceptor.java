package com.ddf.boot.common.websocket.interceptor;

import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.core.util.SpringContextHolder;
import com.ddf.boot.common.core.util.WebUtil;
import com.ddf.boot.common.core.util.SecureUtil;
import com.ddf.boot.common.websocket.constant.WebsocketConst;
import com.ddf.boot.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.WebSocketSessionWrapper;
import com.ddf.boot.common.websocket.service.MerchantBaseDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;

/**
 * 握手前的拦截器，认证客户端是否允许握手
 * WebSocketHandlerDecorator
 * WebSocketHttpRequestHandler
 *

 * @date 2019/8/20 11:46
 */
@Slf4j
public class CustomizeHandshakeInterceptor implements HandshakeInterceptor {

    private Map<String, HandshakeDowngrade> handshakeDowngradeMap = SpringContextHolder.getApplicationContext().getBeansOfType(HandshakeDowngrade.class);

    private MerchantBaseDeviceService merchantBaseDeviceService = SpringContextHolder.getBean(MerchantBaseDeviceService.class);

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
     *
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        ServletServerHttpRequest req = (ServletServerHttpRequest) request;
        HttpServletRequest servletRequest = req.getServletRequest();
        Principal principal = req.getPrincipal();
        if (principal != null) {
            return true;
        }

        AuthPrincipal authPrincipal = null;

        String header = servletRequest.getHeader(WebsocketConst.TOKEN_PARAMETER);
        if (StringUtils.isNotBlank(header)) {
            try {
                header = SecureUtil.privateDecryptFromBcd(header);
                HandshakeParam handshakeParam = JsonUtil.toBean(header, HandshakeParam.class);
                validArgument(handshakeParam);

                if (AuthPrincipal.LoginType.ANDROID.equals(handshakeParam.getLoginType())) {
                    if (merchantBaseDeviceService.isValid(handshakeParam.getDeviceNumber(), handshakeParam.getRandomCode())) {
                        authPrincipal = AuthPrincipal.buildAndroidAuthPrincipal(handshakeParam.getRandomCode(),
                                handshakeParam.getDeviceNumber(), handshakeParam.getVersion(), handshakeParam.getCurrentTimeStamp());
                    }
                }
            } catch (Exception e) {
                log.error("认证参数反序列化失败！参数为: " + ExceptionUtils.getStackTrace(e), header);
            }
        }

        // 当前主版本的验证流程如果走不同，提供一个降级接口，里面可以覆盖之前的处理结果，可以使用老版本的握手处理
        if (authPrincipal == null && handshakeDowngradeMap != null && !handshakeDowngradeMap.isEmpty()) {
            for (Map.Entry<String, HandshakeDowngrade> entry : handshakeDowngradeMap.entrySet()) {
                HandshakeDowngrade handshakeDowngrade = entry.getValue();
                AuthPrincipal result = handshakeDowngrade.validPrincipal(servletRequest, response);
                if (result != null) {
                    authPrincipal = result;
                    break;
                }
            }
        }

        if (authPrincipal == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // 与Nginx约定，Nginx需要将真实的ip放到请求头中
        if (StringUtils.isNotBlank(request.getHeaders().getFirst(REAL_IP_HEADER))) {
            attributes.put(WebsocketConst.CLIENT_REAL_IP, request.getHeaders().getFirst(REAL_IP_HEADER));
        } else {
            attributes.put(WebsocketConst.CLIENT_REAL_IP, req.getRemoteAddress().getAddress().getHostAddress());
        }
        attributes.put(WebsocketConst.SERVER_IP, WebUtil.getHost());
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
                oldSession.getWebSocketSession().sendMessage(new TextMessage("账号在别处[" +
                        newClientAddress + "]登陆，即将断开连接。。。"));
                oldSession.getWebSocketSession().close();
            } catch (Exception ignored) {
                log.error(ExceptionUtils.getStackTrace(ignored));
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
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }


    private static boolean validArgument(HandshakeParam handshakeParam) {
        if (StringUtils.isAnyBlank(handshakeParam.getDeviceNumber(), handshakeParam.getRandomCode())) {
            log.error("设备号和随机码都不能为空！, {}, {}", handshakeParam.getDeviceNumber(), handshakeParam.getRandomCode());
            return false;
        }

        if (handshakeParam.getLoginType() == null) {
            log.error("登录类型不能为空！");
            return false;
        }

        long now = System.currentTimeMillis();
        if (Math.abs(now - handshakeParam.getCurrentTimeStamp()) > WebsocketConst.VALID_AUTH_TIMESTAMP) {
            log.error("认证参数已过期{}==>{}", now, handshakeParam.getCurrentTimeStamp());
            return false;
        }
        return true;
    }
}
