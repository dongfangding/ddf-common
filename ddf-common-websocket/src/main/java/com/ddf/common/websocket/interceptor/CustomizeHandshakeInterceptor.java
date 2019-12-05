package com.ddf.common.websocket.interceptor;

import com.ddf.common.util.SecureUtil;
import com.ddf.common.util.SpringContextHolder;
import com.ddf.common.util.WebUtil;
import com.ddf.common.websocket.constant.WebsocketConst;
import com.ddf.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.common.websocket.model.ws.AuthPrincipal;
import com.ddf.common.websocket.model.ws.WebSocketSessionWrapper;
import com.ddf.common.websocket.service.MerchantBaseDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
 * @author dongfang.ding
 * @date 2019/8/20 11:46
 */
@Slf4j
public class CustomizeHandshakeInterceptor implements HandshakeInterceptor {

    private MerchantBaseDeviceService merchantBaseDeviceService = SpringContextHolder.getBean(MerchantBaseDeviceService.class);

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
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        ServletServerHttpRequest req = (ServletServerHttpRequest) request;
        HttpServletRequest servletRequest = req.getServletRequest();
        Principal principal = req.getPrincipal();
        if (principal != null) {
            return true;
        }
        String token = servletRequest.getParameter(WebsocketConst.TOKEN_PARAMETER);
        if (StringUtils.isBlank(token)) {
            return false;
        }
        String encryptToken;
        try {
            encryptToken = SecureUtil.privateDecryptFromBcd(token);
        } catch (Exception e) {
            log.error("{}=>解密出错，不允许认证！", token, e);
            return false;
        }

        String[] encryptInfo = encryptToken.split(WebsocketConst.AUTH_SPLIT);
        // loginType_ime_token
        if (encryptInfo.length != 4) {
            log.error("解析后格式不符合！=> {} <==", encryptToken);
            return false;
        }

        String loginType = encryptInfo[0];
        if (StringUtils.isBlank(loginType)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        AuthPrincipal.LoginType loginTypeEnum;
        try {
            loginTypeEnum = AuthPrincipal.LoginType.valueOf(loginType);
        } catch (Exception e) {
            log.error("loginType的值不正确！！【{}】", loginType);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        boolean handshakeResult = false;
        String ime = encryptInfo[1];
        token = encryptInfo[2];
        long timestamp = Long.parseLong(encryptInfo[3]);
        long now = System.currentTimeMillis();
        if (Math.abs(now - timestamp) > WebsocketConst.VALID_AUTH_TIMESTAMP) {
            log.error("{}认证参数已过期", ime);
            return false;
        }
        AuthPrincipal authPrincipal;
        if (AuthPrincipal.LoginType.ANDROID.equals(loginTypeEnum)) {
            if (merchantBaseDeviceService.isValid(ime, token)) {
                handshakeResult = true;
            }
        } else if (AuthPrincipal.LoginType.APP_ID.equals(loginTypeEnum)) {
            // todo add other type
            return false;
        }

        if (!handshakeResult) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        authPrincipal = new AuthPrincipal(token, ime, loginTypeEnum);
        // 与Nginx约定，Nginx需要将真实的ip放到请求头中
        if (StringUtils.isNotBlank(request.getHeaders().getFirst("X-Real-IP"))) {
            attributes.put(WebsocketConst.CLIENT_REAL_IP, request.getHeaders().getFirst("X-Real-IP"));
        } else {
            attributes.put(WebsocketConst.CLIENT_REAL_IP, req.getRemoteAddress().getAddress().getHostAddress());
        }
        attributes.put(WebsocketConst.SERVER_IP, WebUtil.getHost());
        attributes.put(WebsocketConst.PRINCIPAL_KEY, authPrincipal);

        // 同一个账号多个地方登陆
        WebSocketSessionWrapper oldSession = WebsocketSessionStorage.get(authPrincipal);
        if (oldSession != null && oldSession.getStatus() == 1) {
            try {
                String newClientAddress = req.getRemoteAddress().getAddress().getHostAddress();
                oldSession.getWebSocketSession().sendMessage(new TextMessage("账号在别处[" +
                        newClientAddress + "]登陆，即将断开连接。。。"));
                oldSession.getWebSocketSession().close();
            } catch (Exception e) {
                // 以上通知只是尽可能如果还在线就通知一下
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
}
